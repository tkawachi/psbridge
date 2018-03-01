# PSBridge

Loading [AWS System Manager Parameter Store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-paramstore.html) to Java system properties.

## Usage

1. Download psbridge.jar from the releases page.
1. Add `-javaagent:psbridge.jar=/path1,/path2` to java argument, where `/path1` and `/path2` are paths of parameter store.

PSBridge loads parameters under specified paths into system properties in specified order.

It uses [the default credential provider chain](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html) to access parameter store.

## License

MIT

Bundled AWS SSM SDK and its dependencies follow its license.
