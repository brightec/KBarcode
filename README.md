# KBarcode

[![Bintray](https://img.shields.io/bintray/v/brightec/maven/KBarcode.svg?maxAge=86400&color=2A91D0)](https://bintray.com/brightec/maven/KBarcode)

A library to help implement barcode scanning.

<br />
<br />
<div align="center">
  <img width="600" src="https://raw.githubusercontent.com/brightec/KBarcode/master/LogoLarge.png">
</div>
<br />
<br />

## Why?

Another barcode library. Yawn.

We can understand why you may think that, but there are some key reasons we decided to write a new barcode library.

-  **Quality** We want this library to be a high quality production ready library.
-  **Camera2** Many barcode libraries still use camera1 API's. These are now deprecated and although unlikely to be removed, you can get better performance and stability from camera2. You are also safe in the knowledge that Android will work to fix issues, and the library will have more longevity.
-  **Firebase** This library uses Firebase to process the frames and return barcodes. The Firebase team are committed to these API's and continue to work to improve them.
-  **Tested** We want this library to have tests. It's surprising how many don't.
-  **Simple** We want the implementation to be simple, but not try to hide away too much of the complexity of the task.

## Download

```
implementation 'uk.co.brightec.kbarcode:kbarcode:$version'
```

## Wiki

For a detailed look at the library and a full get started guide checkout the [wiki](https://github.com/brightec/KBarcode/wiki)

## Community

We welcome community involvement with this library. We want this library to be useful for others, and of a high production quality.

### Issues

Please do raise issues if you find problems with the library, sample or its documentation. We have provided a template to use.

### Pull Requests

If you find an issue, why not try to fix it and create a pull request. We run CI checks on every pull request which must pass.
To run these locally
```
./gradlew check connectedAndroidTest
```

This will run our [code standards](https://github.com/brightec/Guidelines_Android) checks, lint, tests and instrumented tests.

If you're keen to help, why not fix someone else's issue.

### Feature Requests

You can submit feature requests as issues. As mentioned above we want this library to be simple, high quality and production ready. We therefore may be selective about which features we wish to include in order to achieve these goals.

Before fully coding a feature, why not raise an issue to start a discussion with us.

## License

See [license](LICENSE)

## Author

Alistair Sykes - [Github](https://github.com/alistairsykes) [Medium](https://medium.com/@alistairsykes) [Twitter](https://twitter.com/SykesAlistair)

This library is maintained by the [Brightec](https://www.brightec.co.uk/) team
