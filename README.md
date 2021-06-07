
# Client-Server app

"# postpc-2021-ex10" 
I pledge the highest level of ethical principles in support of academic excellence.  
I ensure that all of my work reflects my own abilities and not those of someone else.

## Question
As a client, at the first time you got a token. You have 2 options:
a. save the token locally (for example in a file or in SP)
b. use this token only in this current app-launch, and re-request a token from the server each time the app launches again.
Write at least 1 pro and 1 con for each implementation (a) & (b).
## Answer
a. Pro: faster and more efficient, no need to get the token from server upon every launch of app.
   Con: If the app's memory is erased, we still have to get it remotely.
b. Pro: Does not rely on local storage, more secure in case token changes.
   Con: Requires constant input from user and input upon every launch.
   

## App flow
Upon the first launch, the app loads a fragment which requests the user's username and enables the button when the input is correct:

<div><img src="screenshots/step_1_a.png" width="50%" height="50%"><img src="screenshots/step_1_b.png" width="50%" height="50%"></div>

Upon click, we show a circular progress indicator until we have received the token from the server:
<img src="screenshots/step_1_c.png" width="50%" height="50%">

When we have our token, we navigate forward to a fragment which displays the user's info.
If the app was already launched in the past, we have a saved token in SharedPreferences and we will navigate directly into this fragment upon
launch.

<img src="screenshots/step_2_a.png" width="50%" height="50%">

If user click on the edit button, we navigate to a fragment which enables him to edit his current "pretty_name" and image:

<img src="screenshots/step_3_a.png" width="50%" height="50%"> <img src="screenshots/step_3_b.png" width="50%" height="50%">

Upon successful edit and POST to the server, we navigate back to the previous fragment, now showing the most updated information:

<img src="screenshots/step_3_c.png" width="50%" height="50%">
