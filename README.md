<p align="center">
  <a href="https://nuggets.life">
    <img src="./assets/nuggets-logo.svg" alt="Nuggets Ltd" width="250">
  </a>
</p>

# Nuggets Mobile SDK (Android)
Mobile (Android) SDK for interaction with the Nuggets Platform.

This SDK provides an Identity Wallet for Self-Sovereign Identity (SSI).

## Install `NuggetsSDK` dependency:
Add the Nuggets SDK dependecy to your project:
```kts
implementation("com.github.NuggetsLtd:mobile-sdk-android:v0.0.1")
```
## Use `NuggetsSDK` in your app:
Update your `Activity` java class file, for example, at the location: `app/src/main/java/{component_path}/MainActivity.kt` (where `component_path` is the path to the class you'd like to add the `NuggetsSDK` screen to).
 
Import the `NuggetsSDKActivity` class:
```kt
import life.nuggets.mobilesdkandroid.MainActivity as NuggetsSDKActivity
```
Extend your activity class from `NuggetsSDKActivity`:
```kt
class MainActivity : NuggetsSDKActivity() {

}
```
