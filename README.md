![Alpha Tiles](/app/src/main/res/drawable/zz_splash.png?raw=true)

*An Android app generator used to build literacy games for minority language communities* 

Learn more on [our website](http://alphatilesapps.org/).

**This program takes a set of files provided by a language community and generates an Android app full of literacy games. We are continually adapting the app generator to accommodate a greater variety of languages and would love to work with yours.**

# Preparing the build files for your app

[English instructions](https://docs.google.com/presentation/d/1w-BTKk2MuJIwTFXfXP8cNShU0QI6MSXM5YJQxcaP4uk/edit#slide=id.p1)

[Spanish instructions](https://docs.google.com/presentation/d/1pjhPZvCVU7T50IdSWVTc0GXgBd24-klR1f3yDuTFhJ0/edit#slide=id.p1)

# Generating the app with Alpha Tiles in Android Studio
#### Windows 11
## Install Android Studio
Download and setup the [latest Android Studio version](https://developer.android.com/studio).

## Clone the source code and sample build assets
Here are the recommended steps for setting up the Alpha Tiles code and loading assets through Git.
1. Immediately after installing Android Studio, select `Clone Repository` in the `Welcome to Android Studio` window. Or, if reopening Android Studio later, click on the five vertical lines in the upper left then select `File`>`New`> `Project from version control...`.
2. If prompted by the warning `Git is not installed`, install [Git](https://git-scm.com/download/win) by clicking on `Download and install`.
3. Then, paste `https://github.com/AlphaTiles/AlphaTiles.git` in the `URL` field and click `Clone`.
4. Next, paste `https://github.com/AlphaTiles/PublicLanguageAssets.git` in the `URL` field and click `Clone`.
5. Going forward, to update the project, click on the Git Branch selector in the upper left of the screen and choose `Update Project...` for the latest code and/or asset changes in these repositories.

## Build your Alpha Tiles app
### Insert the language-specific assets
Click the `Project` tab in the side menu of Android Studio. Then select `Project` from the dropdown at the top of the Project panel. This will show the file structure of the Alpha Tiles source code.

To try Alpha Tiles with sample build assets, use a folder of build assets, such as `tpxTeocuitlapa`, from the [PublicLanguageAssets](https://github.com/AlphaTiles/PublicLanguageAssets) repository. If cloned using [the instructions above](#clone-the-source-code-and-sample-build-assets), the tpxTeocuitlapa folder will be at `C:\Users\(Your user)\StudioProjects\PublicLanguageAssets\tpxTeocuitlapa` on your machine.
Alternatively, put together a build assets folder for a new app, following the [instructions above](#preparing-the-build-files-for-your-app).

Place the build assets folder into the [src](app/src) folder in the Android Studio Alpha Tiles project as a sister to [main](app/src/main).

### Specify the product flavor
In [app/build.gradle](app/build.gradle), remove or comment out any flavors in `productFlavors {}` that don't correspond to your build assets. If using new build assets, create a product flavor for your language. Modify the default product flavor, tpxTeocuitlapa, keeping everything the same except for the following:
* Change the product flavor name to match the build assets folder name.
* The third string after resValue must contain the name of the app as it will be displayed to users: `resValue "string", "app_name", "---Your app name here ---"`

### Sync and run
Lastly, select `File > Sync Project with Gradle Files`. Once the project syncs, try out the app in an emulator or connected device by clicking the green 'play' icon in the top menu. Export an apk or app bundle using the `Build` menu.

### Detailed processing steps
For a detailed guide to assembling, validating and building a new app, follow these detailed [processing steps](https://docs.google.com/document/d/1C93cJrd83B5Cn97azkj34eBQ_bWDTBKHotcCSPQ78uY).

## Analytics
To use [Firebase Analytics](https://firebase.google.com/), add a valid `google-services.json` file at the root of the build assets folder. Unless you are part of the Alpha Tiles development team, generate a google-services.json file using [this tutorial](https://cloud.google.com/firestore/docs/client/get-firebase) from Firebase. 

Or, if not including this file, go to [app/build.gradle](app/build.gradle) and comment out the following three lines:

* `apply plugin: 'com.google.gms.google-services'`

and under `dependencies {}`:

* `implementation platform('com.google.firebase:firebase-bom:33.7.0')`
* `implementation 'com.google.firebase:firebase-analytics'`

## Minimum API level

By default, Alpha Tiles apps run on devices with Android 5.0+ (API 21+).