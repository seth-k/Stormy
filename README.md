# Stormy
Stormy is a weather app for Android. It's originally from Treehouse courses [Build a Weather App] (http://teamtreehouse.com/library/build-a-weather-app) and [Android Lists and Adapters] (http://teamtreehouse.com/library/android-lists-and-adapters). Being modified to use location services and different sources of weather data.

## Weather Sources

Stock Stormy has all the data grabbing and parsing code in the MainActivty alongside the UI. I pulled all of that out to their own classes. It's now an abstract class to fetch new forecast data in the background and a callback interface for when it succeeds/fails. The abstract part handles making the HTTP call and getting the response. The concrete part builds the URL and parses the result for the API of choice. 

Currently just uses Forcast.io just like stock Stormy, but easily extendable to other APIs.

## Location

In progress. 
* Needs a simple location that won't need to be updated very often.
* Needs to go where Google Play Services doesn't. 




