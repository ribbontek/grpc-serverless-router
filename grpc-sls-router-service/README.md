# gRPC Serverless Router Service

### Build the project 

- `./gradlew clean build -i`

### Dockerise the project

In the docker directory, create the certs for the application:

```bash 
mkdir certs && \ 
openssl req -x509 -nodes -subj "/CN=localhost" -newkey rsa:4096 -sha256 -keyout certs/server.key -out certs/server.crt -days 3650
``` 

In the sub-project directory, create the image
- `export IMAGE_TAG=1.0.0 && docker build -t $(basename $PWD):latest -t $(basename $PWD):"${IMAGE_TAG}" -f ./docker/Dockerfile .`

### Publish the docker image to AWS ECR

Specify deployment AWS region:

- `export ECR_REGION="ap-southeast-2"`

Login to ECR via docker:

- `aws ecr get-login-password --region "${ECR_REGION}" --profile "${AWS_PROFILE}" | docker login --username AWS --password-stdin $(aws sts get-caller-identity --query Account --output text).dkr.ecr."${ECR_REGION}".amazonaws.com`

Create the ECR repository if it doesn't exist:

- `aws ecr create-repository --repository-name app/$(basename "$PWD") --region "${ECR_REGION}" --profile "${AWS_PROFILE}"`

Tag & Push the image to ECR:

```bash
export ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text) && \ 
docker tag $(basename "$PWD"):latest "${ACCOUNT_ID}".dkr.ecr."${ECR_REGION}".amazonaws.com/app/$(basename "$PWD"):latest && \ 
docker tag $(basename "$PWD"):"${IMAGE_TAG}" "${ACCOUNT_ID}".dkr.ecr."${ECR_REGION}".amazonaws.com/app/$(basename "$PWD"):"${IMAGE_TAG}" && \ 
docker push "${ACCOUNT_ID}".dkr.ecr."${ECR_REGION}".amazonaws.com/app/$(basename "$PWD"):latest && \ 
docker push "${ACCOUNT_ID}".dkr.ecr."${ECR_REGION}".amazonaws.com/app/$(basename "$PWD"):"${IMAGE_TAG}"
```
