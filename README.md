![Alpha Tiles](/app/src/main/res/drawable/zz_splash.png?raw=true)

*An Android app generator used to build literacy games for minority language communities* 

Learn more on [our website](http://alphatilesapps.org/).

**This program takes a set of files provided by a language community and generates an Android app full of literacy games. We are continually adapting the app generator to accommodate a greater variety of languages and would love to work with yours.**

# Preparing the build files for your app

[English instructions](https://docs.google.com/presentation/d/1w-BTKk2MuJIwTFXfXP8cNShU0QI6MSXM5YJQxcaP4uk/edit#slide=id.p1)

[Spanish instructions](https://docs.google.com/presentation/d/1pjhPZvCVU7T50IdSWVTc0GXgBd24-klR1f3yDuTFhJ0/edit#slide=id.p1)

# Generating the app with Alpha Tiles in Android Studio
#### Windows 10/11
## Install Java and Android Studio
The Alpha Tiles build gradle requires Android Studio Flamingo and Java JDK 17. [These instructions](https://www.makeuseof.com/windows-android-studio-setup/#:~:text=Before%20installing%20Android%20Studio%2C%20you,for%20creating%20Java%2Dbased%20applications.) cover all the steps for first-time installation of Java and Android Studio.

## Clone the source code and sample build assets
Here is the recommended route for interacting with Alpha Tiles code and build assets through Git.
1. Have [Git installed](https://git-scm.com/download/win) on your machine.
2. In Android Studio, select `File`>`New`> `Project from version control...`, paste in `https://github.com/AlphaTiles/AlphaTiles.git` for `URL`, and click `Clone`.
3. For sample build assets, repeat step 2, pasting in `https://github.com/AlphaTiles/PublicLanguageAssets.git`.
4. Going forward, click the blue down arrow in Android Studio to `Update Project...` for the latest code and/or asset changes in these repositories.


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
For a detailed guide to assembling, validating and building a new app, follow these detailed [processing steps](https://docs.google.com/document/d/1q8P81o_3eIw2dJHbx4smEWO_pT8_q5Svxvh_dHzJTEY).

## Analytics
To use [Firebase Analytics](https://firebase.google.com/), add a valid `google-services.json` file at the root of the build assets folder. Unless you are part of the Alpha Tiles development team, generate a google-services.json file using [this tutorial](https://cloud.google.com/firestore/docs/client/get-firebase) from Firebase. 

Or, if not including this file, go to [app/build.gradle](app/build.gradle) and comment out the following three lines:

* `apply plugin: 'com.google.gms.google-services'`

and under `dependencies {}`:

* `implementation platform('com.google.firebase:firebase-bom:25.12.0')`
* `implementation 'com.google.firebase:firebase-analytics'`

## Minimum API levels

By default, Alpha Tiles apps run on devices with Android 5.0+ (API 21+). 

Tailoring the features below would tailor the minimum API level required.
* API 16 = Jelly Bean (4.1) - required for Firebase
* API 17 = Jelly Bean MR1 (4.2) - required for forcing RTL/LTR layout direction
* API 21 = Lollipop (5.0) - required for correct display of special characters in TextViews without Grandroid, etc.
