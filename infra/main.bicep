@description('Short prefix used for the Azure resource names.')
@minLength(3)
@maxLength(24)
param appName string = 'galerique-retake'

@description('Azure region for all resources.')
param location string = resourceGroup().location

@description('Tags applied to the resources created by this template.')
param tags object = {
  application: 'galerique'
  environment: 'retake'
  managedBy: 'bicep'
}

@description('GitHub account that owns the Galerique container images.')
param containerImageOwner string = 'misbhvv'

@description('Container image tag to deploy.')
param containerImageTag string = 'latest'

@description('Database name used by the Spring Boot application.')
param mongoDatabaseName string = 'cloudnativeengineeringproject'

var suffix = uniqueString(subscription().subscriptionId, resourceGroup().id)
var storageAccountName = take('gal${suffix}', 24)
var functionAppName = take(toLower('${appName}-thumbnails-${suffix}'), 60)
var functionContentShare = take(toLower(replace('${appName}${suffix}fn', '-', '')), 63)
var thumbnailQueueName = 'thumbnail-jobs'
var artworkContainerName = 'artworks'
var storageConnectionString = 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};EndpointSuffix=${environment().suffixes.storage};AccountKey=${storageAccount.listKeys().keys[0].value}'
var mongoConnectionString = replace(mongoAccount.listConnectionStrings().connectionStrings[0].connectionString, '/?', '/${mongoDatabaseName}?')
var backendImage = 'ghcr.io/${toLower(containerImageOwner)}/cne-galerique-backend:${containerImageTag}'
var frontendImage = 'ghcr.io/${toLower(containerImageOwner)}/cne-galerique:${containerImageTag}'
var backendAppName = take('${appName}-backend-${suffix}', 32)
var frontendAppName = take('${appName}-frontend-${suffix}', 32)
var frontendUrl = 'https://${frontendAppName}.azurewebsites.net'

resource storageAccount 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: storageAccountName
  location: location
  tags: tags
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    accessTier: 'Hot'
    allowBlobPublicAccess: true
    allowSharedKeyAccess: true
    minimumTlsVersion: 'TLS1_2'
    supportsHttpsTrafficOnly: true
  }
}

resource blobService 'Microsoft.Storage/storageAccounts/blobServices@2023-05-01' = {
  parent: storageAccount
  name: 'default'
}

resource artworkContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-05-01' = {
  parent: blobService
  name: artworkContainerName
  properties: {
    publicAccess: 'Blob'
  }
}

resource queueService 'Microsoft.Storage/storageAccounts/queueServices@2023-05-01' = {
  parent: storageAccount
  name: 'default'
}

resource thumbnailQueue 'Microsoft.Storage/storageAccounts/queueServices/queues@2023-05-01' = {
  parent: queueService
  name: thumbnailQueueName
}

resource mongoAccount 'Microsoft.DocumentDB/databaseAccounts@2024-11-15' = {
  name: take(toLower('${appName}-mongo-${suffix}'), 44)
  location: location
  tags: tags
  kind: 'MongoDB'
  properties: {
    databaseAccountOfferType: 'Standard'
    apiProperties: {
      serverVersion: '4.2'
    }
    capabilities: [
      {
        name: 'EnableMongo'
      }
      {
        name: 'EnableServerless'
      }
    ]
    consistencyPolicy: {
      defaultConsistencyLevel: 'Session'
    }
    locations: [
      {
        locationName: location
        failoverPriority: 0
        isZoneRedundant: false
      }
    ]
    minimalTlsVersion: 'Tls12'
    publicNetworkAccess: 'Enabled'
  }
}

resource mongoDatabase 'Microsoft.DocumentDB/databaseAccounts/mongodbDatabases@2024-11-15' = {
  parent: mongoAccount
  name: mongoDatabaseName
  properties: {
    resource: {
      id: mongoDatabaseName
    }
  }
}

resource appServicePlan 'Microsoft.Web/serverfarms@2023-12-01' = {
  name: take('${appName}-web-plan-${suffix}', 60)
  location: location
  tags: tags
  kind: 'linux'
  sku: {
    name: 'B1'
    tier: 'Basic'
    capacity: 1
  }
  properties: {
    reserved: true
  }
}

resource backendApp 'Microsoft.Web/sites@2023-12-01' = {
  name: backendAppName
  location: location
  tags: tags
  kind: 'app,linux,container'
  properties: {
    serverFarmId: appServicePlan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'DOCKER|${backendImage}'
      ftpsState: 'Disabled'
      minTlsVersion: '1.2'
      appSettings: [
        {
          name: 'WEBSITES_PORT'
          value: '8080'
        }
        {
          name: 'SPRING_PROFILES_ACTIVE'
          value: 'prod'
        }
        {
          name: 'SERVER_PORT'
          value: '8080'
        }
        {
          name: 'MONGODB_URI'
          value: mongoConnectionString
        }
        {
          name: 'AZURE_STORAGE_CONNECTION_STRING'
          value: storageConnectionString
        }
        {
          name: 'AZURE_STORAGE_CONTAINER'
          value: artworkContainer.name
        }
        {
          name: 'THUMBNAIL_QUEUE_NAME'
          value: thumbnailQueue.name
        }
        {
          name: 'FRONTEND_URLS'
          value: frontendUrl
        }
      ]
    }
  }
}

resource frontendApp 'Microsoft.Web/sites@2023-12-01' = {
  name: frontendAppName
  location: location
  tags: tags
  kind: 'app,linux,container'
  properties: {
    serverFarmId: appServicePlan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'DOCKER|${frontendImage}'
      ftpsState: 'Disabled'
      minTlsVersion: '1.2'
      appSettings: [
        {
          name: 'WEBSITES_PORT'
          value: '3000'
        }
        {
          name: 'NODE_ENV'
          value: 'production'
        }
      ]
    }
  }
}

resource functionPlan 'Microsoft.Web/serverfarms@2023-12-01' = {
  name: take('${appName}-functions-plan-${suffix}', 60)
  location: location
  tags: tags
  kind: 'functionapp'
  sku: {
    name: 'Y1'
    tier: 'Dynamic'
  }
  properties: {
    reserved: true
  }
}

resource thumbnailFunction 'Microsoft.Web/sites@2023-12-01' = {
  name: functionAppName
  location: location
  tags: tags
  kind: 'functionapp,linux'
  properties: {
    serverFarmId: functionPlan.id
    httpsOnly: true
    siteConfig: {
      alwaysOn: false
      ftpsState: 'Disabled'
      linuxFxVersion: 'Java|21'
      minTlsVersion: '1.2'
      appSettings: [
        {
          name: 'AzureWebJobsStorage'
          value: storageConnectionString
        }
        {
          name: 'WEBSITE_CONTENTAZUREFILECONNECTIONSTRING'
          value: storageConnectionString
        }
        {
          name: 'WEBSITE_CONTENTSHARE'
          value: functionContentShare
        }
        {
          name: 'FUNCTIONS_EXTENSION_VERSION'
          value: '~4'
        }
        {
          name: 'FUNCTIONS_WORKER_RUNTIME'
          value: 'java'
        }
        {
          name: 'JAVA_OPTS'
          value: '-Djava.awt.headless=true'
        }
        {
          name: 'THUMBNAIL_QUEUE_NAME'
          value: thumbnailQueue.name
        }
        {
          name: 'BLOB_CONTAINER_NAME'
          value: artworkContainer.name
        }
      ]
    }
  }
}

output functionAppName string = thumbnailFunction.name
output storageAccountName string = storageAccount.name
output artworkContainerName string = artworkContainer.name
output thumbnailQueueName string = thumbnailQueue.name
output mongoAccountName string = mongoAccount.name
output backendUrl string = 'https://${backendApp.properties.defaultHostName}'
output frontendUrl string = 'https://${frontendApp.properties.defaultHostName}'
