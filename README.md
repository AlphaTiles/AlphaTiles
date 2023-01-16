![Alpha Tiles](/app/src/main/res/drawable/zz_splash.png?raw=true)

*An Android app generator used to build literacy games for minority language communities*

Learn more on [our website](http://alphatilesapps.org/).

**This program takes a set of files provided by a language community and generates an Android app full of literacy games. We are continually adapting the app generator to accommodate a greater variety of languages and would love to work with yours.**

## Preparing the build files for your Alpha Tiles app

[English instructions](https://docs.google.com/presentation/d/1w-BTKk2MuJIwTFXfXP8cNShU0QI6MSXM5YJQxcaP4uk/edit#slide=id.p1)

[Spanish instructions](https://docs.google.com/presentation/d/1pjhPZvCVU7T50IdSWVTc0GXgBd24-klR1f3yDuTFhJ0/edit#slide=id.p1)

## Building an Alpha Tiles app

To build an app in your language, put your build files into a language-specific directory [at the same level as /app/src/main](productFlavorsInAlphaTiles.pdf).

Then, edit [app/build.gradle](app/build.gradle) with the details of your build assets:  In the section on `productFlavors`, remove all entries that don't match your language.  Alternatively, you may wish to edit one of them (as a template) to match your assets.  Your product flavor entry's name must match the name of your build assets folder.

Lastly, before building the app for an emulator or device, select `File > 'Sync Project with Gradle Files'`.


## Analytics
To use [Firebase Analytics](https://firebase.google.com/), you must provide a valid `google-services.json` file. Unless you are part of the Alpha Tiles development team, generate one for yourself using [this tutorial](https://cloud.google.com/firestore/docs/client/get-firebase) from Firebase. 

Then, add this line to the top of app/build.gradle:

* apply plugin: 'com.google.gms.google-services'

and add these lines to dependencies { }:

* implementation platform('com.google.firebase:firebase-bom:25.12.0')
* implementation 'com.google.firebase:firebase-analytics'


## Sample build assets

You may wish to use one or more of our publicly-available [build asset bundles](https://github.com/AlphaTiles/PublicLanguageAssets) as samples.  You will need to provide your own `google-services.json` file to use these build assets.

## Minimum API levels

By default, the code in this repository runs on a minimum API level of 21. Tailoring the features below would tailor the minimum API level required.

* API 16 = Jelly Bean (4.1) - required for Firebase
* API 17 = Jelly Bean MR1 (4.2) - required for forcing RTL/LTR layout direction
* API 21 = Lollipop (5.0) - required for correct display of special characters in TextViews without Grandroid, etc.
