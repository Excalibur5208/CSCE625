intersection([],_,Tmp,V) :-
	V=Tmp.
intersection([H|T],List2,Tmp,V) :-
	member(H,List2),
	append(Tmp,[H],TmpNew),
	intersection(T,List2,TmpNew,V).
intersection([H|T],List2,Tmp,V) :-
	\+ member(H,List2),
	intersection(T,List2,Tmp,V).

intersection(List1,List2,V) :-
	intersection(List1,List2,[],V).
intersection(_,_,_).