# Stormy
Stormy is a weather app for Android. It's originally from Treehouse courses
[Build a Weather App] (http://teamtreehouse.com/library/build-a-weather-app) and
[Android Lists and Adapters] (http://teamtreehouse.com/library/android-lists-and-adapters).
Modified to use location services and different sources of weather data.

## Weather Sources

Stock Stormy has all the data grabbing and parsing code in the MainActivty alongside the UI. I
pulled all of that out to their own classes. It's now an abstract class to fetch new forecast data
in the background and a callback interface for when it succeeds/fails. The abstract part handles
making the HTTP call and getting the response. The concrete part builds the URL and parses the
result for the API of choice.

Currently just uses Forcast.io just like stock Stormy, but easily extendable to other APIs.

## Location

There are 2 basic requirements I had for getting the current location.

* Needs a simple location that won't need to be updated very often.
* Needs to go where Google Play Services doesn't.

That second requirement can need a bit of extra code to replace what the Play Services location API
does for you
Luckily the Search-GitHub-for-a-Solution solution uncovered a nice and simple one at
https://github.com/mrmans0n/smart-location-lib that can use the Google Play location service when
available but fall back gracefully when it's not.








