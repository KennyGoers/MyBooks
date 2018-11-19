# My Books
Coding Challenge (details left out to protect the assigner)

This coding challenge was attacked in phases and more work will be done to improve it wether or not I'm selected to move on. Part of the challenge was to minimimze usage of 3rd party code, I chose to utilize the following 3rd party components:

Retrofit for consuming RESTful service APIs
Picasso for image download and display (because having written one years ago, no one should have to deal with those details on every new project)
Kotterknife (a code file, Kotlin butterknife, for clearer code and the Kotlin extensions do not work that great)
RxJava for a LOT of reasons

I consider all these must haves to avoid a lot of re-inventing the wheel

The task was approached in the following manner:

* Evaluate as much as possible the constraints given.
* Compare those constraints to data provided by the APIs for OpenBook
* Create an simple Kotlin App project from the Wizard
* Migrate the project to support AndroidX (recently released)
* Define the basic Material constants (colors, etc)
* Wrap the network APIs for Search
* Iterate on a UX pattern that suits the requirements as I interpreted them.

All the base requirements have been met as I interpreted them, far more than ten hours were put into this effort, the value in this for me was evaluating parts of the newly organized Android Support components and Material Design components, theywere initially released in April at Google IO, but evolved somewhat since then and NO ONE knows every component well enough to not spend time grokking it.

Some of the things I have not done, but will over time:
* Fully utilize the Google ViewModel and LiveData interfaces from the architecture components. In starting this project I knew some about them, but they are new to me, not enough to try them and risk not getting anything working. Data access and API access should be moved to utilize that architecture, but I am unable to do it well in the time given, UX needs to be solidified before ViewModels are helpful as constantly changing them when trying to organize a good UI is time consuming.
* Buy/Search for Online functionality
* Book Detail (a lot of detail is available for each book, a detail view would be interesting)

I look forward to discussing the architecture and implementation with you

