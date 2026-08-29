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

var suffix = uniqueString(subscription().subscriptionId, resourceGroup().id)
var storageAccountName = take('gal${suffix}', 24)
var functionAppName = take(toLower('${appName}-thumbnails-${suffix}'), 60)
var functionContentShare = take(toLower(replace('${appName}${suffix}fn', '-', '')), 63)
var thumbnailQueueName = 'thumbnail-jobs'
var artworkContainerName = 'artworks'
var storageConnectionString = 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};EndpointSuffix=${environment().suffixes.storage};AccountKey=${storageAccount.listKeys().keys[0].value}'

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
