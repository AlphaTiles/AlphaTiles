![Alpha Tiles](/app/src/main/res/drawable/zz_splash.png?raw=true)

This program is an Android app generator used to build literacy games for minority language communities (for Android). Learn more at http://alphatilesapps.org/.

Alpha Tiles requires a set of build files (word list, audio, images, etc.) for the desired language. The design goal is that this program should be able to generate these games for any language that prepares the necessary language definitions and media, with no need for a language community to manage actual code. As we work with new language build files, adaptations will be made to make the app generator program flexible enough to handle new language-specific challenges.

# Preparing the build files for an Alpha Tiles app

English instructions: https://docs.google.com/presentation/d/1w-BTKk2MuJIwTFXfXP8cNShU0QI6MSXM5YJQxcaP4uk/edit#slide=id.p1
Spanish instructions: https://docs.google.com/presentation/d/1pjhPZvCVU7T50IdSWVTc0GXgBd24-klR1f3yDuTFhJ0/edit#slide=id.p1

# Building an Alpha Tiles app

In order to build a literacy game for your language using the Alpha Tiles engine, you'll need to copy its resources into the correct location - a language-specific directory at the same level as /app/src/main.

A visual reference for this is available as [productFlavorsInAlphaTiles.pdf](productFlavorsInAlphaTiles.pdf).

As Alpha Tiles uses [Firebase](https://firebase.google.com/), a valid `google-services.json` file must also be provided.  You may wish to use Firebase's "Get started" tutorial in order to generate one for yourself unless you are part of the Alpha Tiles development team.

Finally, you will need to adjust [app/build.gradle](app/build.gradle) with details specific to  your build assets.  Look for the section on `productFlavors` and remove all entries that you do not have available build assets for.  That said, you may wish to preserve one and edit it (as a template) for use with your assets.  Note that each entry's name must exactly match the name of that product flavor's main folder.

After cloning the repository and saving the language assets in the src folder of the app, click on the "Build Variants" tab in the bottom left of Android Studio. To configure and build the app with the flavor of language assets just installed, select "{productFlavor}Debug" from the "Active Build Variant" drop-down selector in the Build Variants window.


## Sample build assets

You may wish to use one or more of our publicly-available build asset bundles found at https://github.com/AlphaTiles/PublicLanguageAssets as samples.  Note that you will still need to provide your own `google-services.json` file to use these build assets.
