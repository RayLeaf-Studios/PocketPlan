## Contributing to PocketPlan
Contributions are always welcome! Take a look at the [firstcontributions repository](https://github.com/firstcontributions/first-contributions), to learn how to fork, make changes and send a PR.
### Branch 
All PRs should be targeting the `dev` branch.
### Procedure
If you are planning to implement a new functionality, or to make other major changes, create an [issue](https://github.com/estep248/PocketPlan/issues) so we know what is being worked on, and have the ability to discuss / comment on the idea.
For minor changes / bug fixes, a PR with an explanatory comment is sufficient.

### Styleguide
Ideally your code is written in a way, that doesn't require comments to understand it, e.g. descriptive variable names, minimal nesting etc.

Use camel casing for variable names e.g. `birthdaysToday`

When referencing a UI component, the first letters of the variable name should identify the type of the UI element. e.g. `tvBirthday` references the birthday TextView

Here are the most used abbreviations:
| Element | Abbreviation |
|--|--|
| TextView | tv |
| ConstraintLayout| cl|
| Button| btn|
| Icon| ic|
| CardView| cv|
| EditText| et|
| Spinner| sp|

Finally, always try to reduce nesting. Instead of 
```
fun foo(){
	if(condition){
		doSomething()
	}
}
```
we prefer
```
fun foo(){
	if(!condition) return
	doSomething()
}
```


