# Azure deployment

`main.bicep` defines the Azure infrastructure used by Galerique.

## Cost controls

- Azure Functions uses the Consumption (`Y1`) plan.
- Cosmos DB uses serverless capacity.
- Storage uses locally redundant `Standard_LRS` storage.
- The frontend and backend share one Basic (`B1`) App Service plan.
- All resources are deployed in one region.

The Basic App Service plan is billed while it exists. Delete the resource group after the assessment when the application is no longer needed.

## Prerequisites

- Azure CLI with Bicep;
- Java 21, Maven, and Azure Functions Core Tools 4;
- access to the GitHub repository and its Actions workflows;
- public frontend and backend packages in GitHub Container Registry.

## Validate the template

Compiling the template does not create Azure resources:

```powershell
az bicep build --file infra/main.bicep --stdout
```

## Create the infrastructure

Select the Azure for Students subscription and create the resource group:

```powershell
az account set --subscription '<subscription-id>'
az group create --name 'galerique-retake-rg' --location 'francecentral'
```

Preview the deployment:

```powershell
az deployment group what-if `
  --resource-group 'galerique-retake-rg' `
  --template-file infra/main.bicep `
  --parameters location='francecentral'
```

Create or update the resources:

```powershell
az deployment group create `
  --name 'galerique-course-infrastructure' `
  --resource-group 'galerique-retake-rg' `
  --template-file infra/main.bicep `
  --parameters location='francecentral' `
  --query properties.outputs
```

The output contains the Function App name, storage account name, Cosmos DB account name, and both Web App URLs. It does not output connection strings.

## Build the Web App images

The frontend needs the deployed backend URL while its Docker image is built. Store the `backendUrl` output as the repository secret and run both existing image workflows:

```powershell
gh secret set NEXT_PUBLIC_BACKEND_URL --body '<backend-url>'
gh workflow run main_galerique-backend.yml
gh workflow run main_galerique.yml
```

When both packages are public in GitHub Container Registry, restart the Web Apps so they pull the current images:

```powershell
az webapp list --resource-group 'galerique-retake-rg' --query '[].name' --output tsv
az webapp restart --resource-group 'galerique-retake-rg' --name '<backend-web-app-name>'
az webapp restart --resource-group 'galerique-retake-rg' --name '<frontend-web-app-name>'
```

## Deploy the Function code

Use the Function App name returned by Bicep:

```powershell
cd thumbnail-function
mvn clean package azure-functions:deploy `
  -DfunctionResourceGroup='galerique-retake-rg' `
  -DfunctionAppName='<function-app-name>'
```

## Verify the application

1. Open the backend Swagger URL and verify that the API responds.
2. Open the frontend Web App and log in or register.
3. Upload an artwork image.
4. In the Azure Storage browser, verify the original under `artworks/<artwork-id>/`.
5. Verify the generated thumbnail under `artworks/<artwork-id>/thumbnails/`.
6. Verify that the `thumbnail-jobs` queue returns to zero messages.

## Cleanup

Deleting the resource group deletes the complete retake environment:

```powershell
az group delete --name 'galerique-retake-rg'
```

Only run cleanup after the assessment or when the Azure deployment is no longer needed.
