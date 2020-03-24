# SimpleDiet
Simpler than calorie counting, more useable than a strict diet plan.

This app is designed to aid in your dieting plans, without overcomplicating the whole process. Rather than tracking every individual calorie you intake, this app tracks that you are eating from every food group, and encourages you to make personal judgements about how unhealthy each meal is for you. it will then keep you on track to eat the correct amoumts from each food group, and to maintain a healthy diet.

Features :
 - Tracks how much you are eating from each food group, Vegetables, Proteins, Dairy, Fruit and Grains.
 - Tracks how much water you are drinking each day, as well as tracking caffeine and alcohol intake.
 - Tracks how unhealthy each meal is, based on your personal judgement, making you more concsious of your decisions, and encourages you to keep your weekly intake of unhealthy food to a minimum
 - Gives warnings and recommendations on your diet as you go
 
 ## Screenshots
 
 ## Features in depth
 
 ### Food Group Tracking
 Simply choose which food groups are in the meal you are about to eat in their relative amounts, no need to get the exact values or to look up on the package or online how many calories are contained in the meal. The app will then track how much of each food group you are getting, and encourage you to have a varied diet with all the nutrients you need.
 
 ### Cheat Tracking
 After adding a meal, simply select how unhealthy you rate it on a scale of 0-3, the app will then track how unhealthy your diet has been over the weeks, and encourage you to make it healthier. By putting the onus on you to rate the healthiness of your meal, we encourage you to become more concsious of the food that you are eating.
 
 ### Drinks Tracking
 When adding a drink you mark down how many serves of water, milk, caffeine and alcohol is contained in it, then the app will track how much hydration you are getting, as well as your weekly intake of caffeine and alcohol.
 
 ### Warnings and Recommendations
 Using the information that you input, the app will give you rcommendations of how to change your diet and warnings when you are approaching your limit of unhealthy meals.
 
 
## Programming Architecture
In the backend the app uses a simple Model View Controller (MVC) Design Pattern, where the user interacts with the View, which contains all the app activities. The View sends commands through to the Controller, which does all the calculations and transformations needed, and interacts with the database contained in the Model.

## Technologies 

### Firebase Firestore
The app uses Firebase Firestore to store all the information from the app, as well as the Firebase Authorisation service to authorise users.. The main purposes of this is for the easy integration of this system with android, and for the flexibility of a NoSQL database. The integration that Firebase has with android allows it to be used offline, means it runs quickly, and allows for further expansions to the product to be easily implemented in the future.

### Notification Manager
The notifications within the app use the default Notification Management system within android, as I felt there was no need to use a library to achieve what we need. The Notifications used by the app are very simple, just notifying the user in the morning and evening, if they turn the feature on. This is achieved by using AlarmManager, which has a method to set a recurring alarm. The recurring alarm goes off at the set times each day, and are picked up by the BroadcastReciever, and trigger a notification to go off.
