# Lambdas

### Install Serverless:

- `sudo npm install -g serverless@latest`

### Build the project:

- `SLS_DEBUG=* sls package`

### Deploy the project:

- `SLS_DEBUG=* serverless deploy --verbose --stage prod`

### Remove the project:

- `SLS_DEBUG=* serverless remove --verbose --stage prod`

### Issues:

If you see any versioning issues, you need to upgrade your global instance of serverless, or specify the exact version in the serverless.yml file -
3.39.0

Update using the command: `sudo npm install -g serverless@latest`

```
Warning: Invalid configuration encountered
at 'provider.runtime': must be equal to one of the allowed values [dotnet6, dotnetcore3.1, go1.x, java11, java8, java8.al2, nodejs12.x, nodejs14.x, nodejs16.x, nodejs18.x, provided, provided.al2, python3.6, python3.7, python3.8, python3.9, ruby2.7]
```

We don't run serverless offline as Java21 is only supported by Serverless Framework v4.

Unfortunately, Serverless Framework v4 is free at a condition and it requires a licensed version & auth setup, having new conditions to its usage.

- https://www.serverless.com/framework/docs-guides-upgrading-v4

### References:

- https://github.com/mbsambangi/aws-java-spring-cloud-function-demo
- https://www.serverless.com/examples/aws-java-spring-cloud-function-demo