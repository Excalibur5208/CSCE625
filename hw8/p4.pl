noConflictA(_,QueueNo,_) :-
	QueueNo =< 1.
noConflictA(A,QueueNo,CurValue) :-
	QueueNo > 1,
	CurValue =\= A,
	(QueueNo - 1) / (CurValue - A) =\= 1,
	(QueueNo - 1) / (CurValue - A) =\= -1.
	
noConflictB(_,QueueNo,_) :-
	QueueNo =< 2.
noConflictB(B,QueueNo,CurValue) :-
	QueueNo > 2,
	CurValue =\= B,
	(QueueNo - 2) / (CurValue - B) =\= 1,
	(QueueNo - 2) / (CurValue - B) =\= -1.

noConflictC(_,QueueNo,_) :-
	QueueNo =< 3.
noConflictC(C,QueueNo,CurValue) :-
	QueueNo > 3,
	CurValue =\= C,
	(QueueNo - 3) / (CurValue - C) =\= 1,
	(QueueNo - 3) / (CurValue - C) =\= -1.
	
noConflictD(_,QueueNo,_) :-
	QueueNo =< 4.
noConflictD(D,QueueNo,CurValue) :-
	QueueNo > 4,
	CurValue =\= D,
	(QueueNo - 4) / (CurValue - D) =\= 1,
	(QueueNo - 4) / (CurValue - D) =\= -1.	
	
	
assignment(_,B,C,D,E,QueueNo,CurValue,ANew,BNew,CNew,DNew,ENew) :-
	QueueNo =:= 1,
	ANew = CurValue,BNew = B,CNew = C,DNew = D,ENew = E.
assignment(A,_,C,D,E,QueueNo,CurValue,ANew,BNew,CNew,DNew,ENew) :-
	QueueNo =:= 2,
	ANew = A,BNew = CurValue,CNew = C,DNew = D,ENew = E.
assignment(A,B,_,D,E,QueueNo,CurValue,ANew,BNew,CNew,DNew,ENew) :-
	QueueNo =:= 3,
	ANew = A,BNew = B,CNew = CurValue,DNew = D,ENew = E.
assignment(A,B,C,_,E,QueueNo,CurValue,ANew,BNew,CNew,DNew,ENew) :-
	QueueNo =:= 4,
	ANew = A,BNew = B,CNew = C,DNew = CurValue,ENew = E.
assignment(A,B,C,D,_,QueueNo,CurValue,ANew,BNew,CNew,DNew,ENew) :-
	QueueNo =:= 5,
	ANew = A,BNew = B,CNew = C,DNew = D,ENew = CurValue.
	
add_next_queen(A,B,C,D,E,QueueNo,_,AAns,BAns,CAns,DAns,EAns) :-
	QueueNo > 5,
	A =\= 0,B =\= 0,C =\= 0,D =\= 0,E =\= 0,
	AAns=A,BAns=B,CAns=C,DAns=D,EAns=E.
add_next_queen(A,B,C,D,E,QueueNo,[],AAns,BAns,CAns,DAns,EAns) :-
	QueueNo =< 5,
	NextQueueNo is QueueNo+1,
	add_next_queen(A,B,C,D,E,NextQueueNo,[1,2,3,4,5],AAns,BAns,CAns,DAns,EAns).
add_next_queen(A,B,C,D,E,QueueNo,[_|T],AAns,BAns,CAns,DAns,EAns) :-
	QueueNo =< 5,
	add_next_queen(A,B,C,D,E,QueueNo,T,AAns,BAns,CAns,DAns,EAns).
add_next_queen(A,B,C,D,E,QueueNo,[CurValue|_],AAns,BAns,CAns,DAns,EAns) :-
	QueueNo =< 5,
	noConflictA(A,QueueNo,CurValue),
	noConflictB(B,QueueNo,CurValue),
	noConflictC(C,QueueNo,CurValue),
	noConflictD(D,QueueNo,CurValue),
	assignment(A,B,C,D,E,QueueNo,CurValue,ANew,BNew,CNew,DNew,ENew),
	NextQueueNo is QueueNo+1,
	add_next_queen(ANew,BNew,CNew,DNew,ENew,NextQueueNo,[1,2,3,4,5],AAns,BAns,CAns,DAns,EAns).
	
queens(A,B,C,D,E) :-
	add_next_queen(0,0,0,0,0,1,[1,2,3,4,5],A,B,C,D,E),
	write('A='),write(A),write(','),tab(1),
	write('B='),write(B),write(','),tab(1),
	write('C='),write(C),write(','),tab(1),
	write('D='),write(D),write(','),tab(1),
	write('E='),write(E),write(';'),nl,fail.
queens(_,_,_,_,_).