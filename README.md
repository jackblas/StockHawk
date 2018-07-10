# Stock Hawk

This is the starter code for project 3 in Udacity's [Android Developer Nanodegree](https://www.udacity.com/course/android-developer-nanodegree-by-google--nd801). 


# Jack B Changes (Udacity Nanodegree Project)

PROJECT SPECIFICATIONS


### Each stock quote on the main screen is clickable and leads to a new screen which graphs the stock's value over time.

•	Added Details Activity with a graph

•	Added company name and 52 weeks range to details



### Stock Hawk does not crash when a user searches for a non-existent stock.

•	Modified QuteSyncJob class to handle invalid symbol.



### Stock Hawk Stocks can be displayed in a collection widget.

•	Added collection widget (big) and one stock widget(small)



### Stock Hawk app has content descriptions for all buttons.

•	Added Description attribute to buttons.

### Stock Hawk app supports layout mirroring using both the RTL attribute and the start/end tags.

•	Added start/end tags to padding and margins



### Strings are all included in the strings.xml file and untranslatable strings have a translatable tag marked to false.

•	Added translatable="false” attribute where required

•	Used a xliff:g tag to mark text that should not be translated



### Other changes:

•	Added currency and number formatting based on locale

•	Added a fix to show the load indicator when RecyclerView is empty

•	Expanded server and connection status check triggered by onRefresh() and the sync job
