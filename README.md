# BazaarUpdater Android SDK

[![GitHub License](https://img.shields.io/github/license/cafebazaar/BazaarPay)](https://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://jitpack.io/v/hamid97m/BazaarUpdater.svg)](https://jitpack.io/#hamid97m/BazaarUpdater)


BazaarUpdater is an Android library that simplifies checking for updates and managing the update process for your application on Bazaar.

## Setup

To get started with BazaarUpdater, you need to add the JitPack repository to your project and include the library dependency.

### Adding JitPack Repository

**Kotlin DSL**

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

**groovy**
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

## Adding Dependency

**Kotlin DSL**

```kotlin
dependencies {
    implementation("com.github.hamid97m:bazaarUpdater:1.0.0-alpha5")
}
```

```groovy
dependencies {
    implementation 'com.github.hamid97m:bazaarUpdater:1.0.0-alpha5'
}
```
## Usage

### Checking for Updates

To check if there are any updates available for your application on Bazaar, use the following code:


```kotlin
BazaarUpdater.getLastUpdateState(context = context) { result ->
    when(result) {
        UpdateResult.AlreadyUpdated -> {
            // Handle the case where the app is already updated
        }
        is UpdateResult.Error -> {
            // Handle the error case
            val errorMessage = result.message
        }
        is UpdateResult.NeedUpdate -> {
            // Handle the case where an update is needed
            val targetVersion = result.targetVersion
        }
    }
}

```

#### Update Result States

##### 1. AlreadyUpdated: Indicates that your application is up-to-date.

##### 2. Error: Indicates an error occurred. Use `result.message` to get the error message.

##### 3. NeedUpdate: Indicates that a new update is available. Use `result.targetVersion` to get the version code of the update.

### Updating the Application

To update your application when a new version is available on Bazaar, simply call:

`BazaarUpdater.updateApplication(context = context)`

## Contributing
Contributions are welcome! If you have suggestions or improvements, please open an issue or submit a pull request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.


