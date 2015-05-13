sqrt(Target,CurrentEst,Tolerance,FinalAnswer) :-
	NewEst is CurrentEst/2 + Target/2/CurrentEst,
	0 =< NewEst - CurrentEst,NewEst - CurrentEst =< Tolerance,
	FinalAnswer = NewEst.
sqrt(Target,CurrentEst,Tolerance,FinalAnswer) :-
	NewEst is CurrentEst/2 + Target/2/CurrentEst,
	-Tolerance =< NewEst - CurrentEst,NewEst - CurrentEst =< 0,
	FinalAnswer = NewEst.
sqrt(Target,CurrentEst,Tolerance,FinalAnswer) :-
	NewEst is CurrentEst/2 + Target/2/CurrentEst,
	sqrt(Target,NewEst,Tolerance,FinalAnswer).

sqrt(0,0).
sqrt(X,Y) :-
	sqrt(X,1,0.0000001,Y).