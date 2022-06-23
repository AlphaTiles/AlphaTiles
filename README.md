![Alpha Tiles](/app/src/main/res/drawable/zz_splash.png?raw=true)

This program is an Android app generator used to build literacy games for minority language communities. Learn more at http://alphatilesapps.org/.

Alpha Tiles requires a set of build files (word list, audio, images, etc.) for the desired language. The design goal is that this program should be able to generate these games for any language that prepares the necessary language definitions and media, with no need for a language community to manage actual code. As we work with new language build files, adaptations will be made to make the app generator program flexible enough to handle new language-specific challenges.

# Preparing the build files for an Alpha Tiles app

English instructions: https://docs.google.com/presentation/d/1w-BTKk2MuJIwTFXfXP8cNShU0QI6MSXM5YJQxcaP4uk/edit#slide=id.p1

Spanish instructions: https://docs.google.com/presentation/d/1pjhPZvCVU7T50IdSWVTc0GXgBd24-klR1f3yDuTFhJ0/edit#slide=id.p1

# Building an Alpha Tiles app

In order to build a literacy game for your language using the Alpha Tiles engine, you'll need to copy its resources into the correct location - a language-specific directory at the same level as /app/src/main.

A visual reference for this is available as [productFlavorsInAlphaTiles.pdf](productFlavorsInAlphaTiles.pdf).

Finally, you will need to adjust [app/build.gradle](app/build.gradle) with details specific to  your build assets.  Look for the section on `productFlavors` and remove all entries that you do not have available build assets for.  That said, you may wish to preserve one and edit it (as a template) for use with your assets.  Note that each entry's name must exactly match the name of that product flavor's main folder.

After cloning the repository and saving the language assets in the src folder of the app, select File > 'Sync Project with Gradle Files' to build the app with the product flavor just selected in the build.gradle.

# Full support of scripts written from right to left (RTL):
If your language has right-to-left script, put "RTL" for "Script direction (LTR or RTL)" in your raw/langinfo.txt file.
Then, in the src/main/res/values/integers.xml file, enter 180 as the value of the integer resource "mirror_flip." This will reflect images so that they point in the proper directions.


# Analytics
To use [Firebase Analytics](https://firebase.google.com/), a valid `google-services.json` file must also be provided. You may wish to use Firebase's "Get started" tutorial in order to generate one for yourself unless you are part of the Alpha Tiles development team. 

Also for Firebase: In app/build.gradle, add this line to the top:

* apply plugin: 'com.google.gms.google-services'

and add these lines to dependencies { }:

* implementation platform('com.google.firebase:firebase-bom:25.12.0')
* implementation 'com.google.firebase:firebase-analytics'


## Sample build assets

You may wish to use one or more of our publicly-available build asset bundles found at https://github.com/AlphaTiles/PublicLanguageAssets as samples.  Note that you will still need to provide your own `google-services.json` file to use these build assets.

# Minimum API levels

By default, the code in this repository runs on a minimum API level of 17. Tailoring the features below would tailor the minimum API level required.

* API 16 = Jelly Bean (4.1) - required for Firebase
* API 17 = Jelly Bean MR1 (4.2) - required for forcing RTL/LTR layout direction
* API 21 = Lollipop (5.0) - required for correct display of special characters in TextViews without Grandroid, etc.
