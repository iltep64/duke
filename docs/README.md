# User Guide

![Screenshot](Ui.png)

Duke is a chat bot, which helps you keep track of your tasks.
You can create new tasks, list and search them, and mark them as done and remove them.

## Features 

- Create new tasks
    - You can keep track of your to-dos, deadlines and events
- List and search them
    - You can list all your tasks, or provide a search phrase to filter them.
- Mark done and delete tasks
    - You can check off tasks you have completed, and remove thme afterwards.

## Usage

### `list` - List all your tasks

This command shows all your current tasks, both incomplete and complete.

Example of usage: 

`list`

Expected outcome:

```
Here are the tasks in your list:
1: [D][✗] homework (by: 19 Sep 2019, 12:00:00 AM)
2: [T][✗] project
```

### `find` - Search through your task list

This command filters through your current tasks.
Only the tasks with descriptions that contain the given phrase will be shown.

`find <phrase>`

Example of usage:

`find work`

Expected outcome:

```
Here are the tasks in your list:
1: [D][✗] homework (by: 19 Sep 2019, 12:00:00 AM)
4: [T][✗] worked example
```

### `todo`, `deadline`, `event` - Create new tasks

These commands create a new incomplete task, and add it to your list.
- `todo` creates a basic to-do task
    - `todo <description>`
- `deadline` creates a deadline task, given a date/time 
    - `deadline <description> /by [<date>|<weekday>] [time]`
- `event` creates a event task, given the date/time of the event
    - `event <description> /at [<date>|<weekday>] [time]`
    
Example of usage:

```
deadline homwork /by Mon
event birthday /at Apr 1 1PM
```

Expected outcome:
```
Got it! I've added this task:
 [D][✗] homework (by: 23 Sep 2019, 12:00:00 AM)
Now you have 3 tasks in your list.

Got it! I've added this task:
 [E][✗] birthday (at: 1 Apr 2020, 1:00:00 PM)
Now you have 4 tasks in your list.
```

### `done` - Mark a task as done

Given the task number, mark that task as completed.

Example of usage:

`done 2`

Expected outcome:

```
Nice! I've marked this task as done.
 [E][✓] birthday (at: 1 Apr 2020, 1:00:00 PM)
```

### `delete` - Delete a task

Given the task number, delete the task.

Example of usage:

`delete 2`

Expected outcome:

```
Nice! I've marked this task as done.
 [E][✓] birthday (at: 1 Apr 2020, 1:00:00 PM)
Now you have 2 tasks in your list.
```


### `help` - Get command help

Show a list of all supported commands.

Example of usage:

`help`

Expected outcome:

```
bye - Exits the program.
bye
deadline - Creates a new deadline task
deadline <Description of task> [ /by (Time of deadline) ]
delete - Deletes a task
delete <Index of task>
done - Marks a task as done.
done <Index of task to delete>
event - Create a event task
event <Description of task> [ /at (Time of event) ]
find - Find tasks that match.
find <String to search for>
help - Shows command help
help
list - Lists all tasks
list
todo - Creates a todo task
todo <Description of task>
```
