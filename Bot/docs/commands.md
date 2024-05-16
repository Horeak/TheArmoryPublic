# Overview
## Generic
- [ping](#ping)
- [status](#status)
- [reminder](#reminder)
- [reminders](#reminders)
## Voice
- [playlist](#playlist)
- [youtube-search](#youtube-search)
- [music-channel](#music-channel)
- [music-history](#music-history)
- [music](#music)
## Destiny
- [destiny-wish](#destiny-wish)
- [destiny-powerlevel](#destiny-powerlevel)
- [destiny-profile](#destiny-profile)
- [destiny](#destiny)
- [destiny-register](#destiny-register)
- [destiny-season](#destiny-season)
## Core
- [timestamp](#timestamp)
- [random](#random)
- [calculator](#calculator)
- [vote](#vote)
- [timezone](#timezone)

---

# Commands
# /youtube-search
Allows searching for YouTube videos to get the url

| Argument | Description | Required |
| --- | --- | --- |
| search | The search phrase to use to find your video | true |

Usage:
> /youtube-search \<search>


---

# /timestamp
Generate a discord timestamp from a given time

| Argument | Description | Required |
| --- | --- | --- |
| time | The time you want the format for. Default time zone is based on GMT | true |

Usage:
> /timestamp \<time>


---

# /random
Generates a random number between the upper and lower limit

#### Sub Commands
<details>
<summary>/random chance</summary>
Simulate a random roll with a specific chance of success

| Argument | Description | Required |
| --- | --- | --- |
| chance | The success chance you want to simulate | true |


Usage:
> /random chance \<chance>
</details>
<details>
<summary>/random between</summary>
Simulate a random number with a upper and lower limit

| Argument | Description | Required |
| --- | --- | --- |
| upper | The upper limit for the number | true |
| lower | The lower limit for the number | false |


Usage:
> /random between \<upper> [lower]
</details>

---

# /destiny-wish
Search up Last wish, wishing wall combinations

Usage:
> /destiny-wish


---

# /calculator
A command for answering simple math equations

| Argument | Description | Required |
| --- | --- | --- |
| input | The math question you want answered. | true |

Usage:
> /calculator \<input>


---

# /reminder
Allows setting up custom reminders

| Argument | Description | Required |
| --- | --- | --- |
| time | When you want to be reminded. | true |
| text | The message you want to be reminded about. | true |

Usage:
> /reminder \<time> \<text>


---

# /destiny-powerlevel
Shows current character power level

Usage:
> /destiny-powerlevel


---

# /destiny-profile
Shows basic account info about someones destiny account

| Argument | Description | Required |
| --- | --- | --- |
| user | Which user you want to see the profile for. | false |

Usage:
> /destiny-profile [user]


---

# /destiny
Look up any item from Destiny 2

| Argument | Description | Required |
| --- | --- | --- |
| item | Which item you are looking for | true |
| public | Show the item in chat | false |
| stats | Show the stats of the item | false |
| perks | Show the perks of the item | false |

Usage:
> /destiny \<item> [public] [stats] [perks]


---

# /destiny-register
Register you destiny account with the bot

Usage:
> /destiny-register


---

# /playlist
#### Sub Commands
<details>
<summary>/playlist delete-playlist</summary>
Delete an entire playlist


Usage:
> /playlist delete-playlist
</details>
<details>
<summary>/playlist add-songs</summary>
Add a new song to an existing playlist

| Argument | Description | Required |
| --- | --- | --- |
| search | The search phrase to use for adding songs | true |


Usage:
> /playlist add-songs \<search>
</details>
<details>
<summary>/playlist delete-songs</summary>
Remove songs from a playlist


Usage:
> /playlist delete-songs
</details>
<details>
<summary>/playlist new-playlist</summary>
Create a new playlist

| Argument | Description | Required |
| --- | --- | --- |
| name | The name of the playlist you wish to create | true |


Usage:
> /playlist new-playlist \<name>
</details>

---

# /vote
Create a custom post for users to be able to vote over

| Argument | Description | Required |
| --- | --- | --- |
| name | The name of the post | true |
| end_time | When will the vote end | true |
| options | A comma seperated list of what options to include, defaults to "Yes,No" | false |

Usage:
> /vote \<name> \<end_time> [options]


---

# /destiny-season
Shows some basic information about the current destiny 2 season.

Usage:
> /destiny-season


---

# /ping
Shows current ping

Usage:
> /ping


---

# /music
Shows how to use the all music commands in the bot

#### Sub Commands
<details>
<summary>/music replay</summary>
Replays a song from your recently played list

| Argument | Description | Required |
| --- | --- | --- |
| count | The amount of songs to go backwards | false |


Usage:
> /music replay [count]
</details>
<details>
<summary>/music current</summary>
Showing the currently playing song in this server


Usage:
> /music current
</details>
<details>
<summary>/music shuffle</summary>
shuffle the current music queue


Usage:
> /music shuffle
</details>
<details>
<summary>/music volume</summary>
Sets the volume of the bot

| Argument | Description | Required |
| --- | --- | --- |
| volume | The volume you want to bot to play at, 0-150% | true |


Usage:
> /music volume \<volume>
</details>
<details>
<summary>/music clear</summary>
clear the current music queue


Usage:
> /music clear
</details>
<details>
<summary>/music resume</summary>
Resumes the currently paused music queue


Usage:
> /music resume
</details>
<details>
<summary>/music repeat</summary>
Toggles repeat for the current music queue in this server


Usage:
> /music repeat
</details>
<details>
<summary>/music play</summary>
Adds songs to the current queue from youtube links and similar

| Argument | Description | Required |
| --- | --- | --- |
| search | The search phrase to use to find music to add | true |


Usage:
> /music play \<search>
</details>
<details>
<summary>/music join</summary>
Add bot to voice channel

| Argument | Description | Required |
| --- | --- | --- |
| channel | The voice channel you want the bot to join | true |


Usage:
> /music join \<channel>
</details>
<details>
<summary>/music skip</summary>
skip the currently playing song


Usage:
> /music skip
</details>
<details>
<summary>/music leave</summary>
Remove bot from voice channel


Usage:
> /music leave
</details>
<details>
<summary>/music queue-playlist</summary>
Add the selected playlist to the queue


Usage:
> /music queue-playlist
</details>
<details>
<summary>/music pause</summary>
Pauses the current music queue


Usage:
> /music pause
</details>
<details>
<summary>/music queue</summary>
Show the current queue for this server

| Argument | Description | Required |
| --- | --- | --- |
| page | Which page of the queue you want to view. | false |


Usage:
> /music queue [page]
</details>

---

# /music-history
Show recently played music

#### Sub Commands
<details>
<summary>/music-history view</summary>
Shows you last 10 played songs on this server


Usage:
> /music-history view
</details>
<details>
<summary>/music-history remove</summary>
Remove a specific track from your history

| Argument | Description | Required |
| --- | --- | --- |
| remove | Which tracks you want to remove from your history | true |


Usage:
> /music-history remove \<remove>
</details>
<details>
<summary>/music-history clear</summary>
Fully clear your recent music history


Usage:
> /music-history clear
</details>

---

# /music-channel
Designate a specific channel to post music related info

#### Sub Commands
<details>
<summary>/music-channel add</summary>
Change what channel to post the currently playing message

| Argument | Description | Required |
| --- | --- | --- |
| channel | Which channel you want to use for music notifications | true |


Usage:
> /music-channel add \<channel>
</details>
<details>
<summary>/music-channel clear</summary>
Remove the currently set music notification channel


Usage:
> /music-channel clear
</details>

---

# /status
Bot status information

Usage:
> /status


---

# /timezone
select which timezone the bot will use, defaults to GMT

| Argument | Description | Required |
| --- | --- | --- |
| timezone | The specific timezone to use | true |

Usage:
> /timezone \<timezone>


---

# /reminders
View all your current reminders

Usage:
> /reminders


---
