calories(water,0).
calories(hamburger,354).
calories(carrot,25).
calories(salad,100).
calories(banana,105).
calories(apple,95).
calories(peanuts,828).
calories(chicken_soup,87).
calories(lasagna,166). % traditional meat lasagna
calories(apple_pie,67).
calories(beans,41).
calories(peas,118).
calories(milk,8).
calories(orange_juice,39).
calories(coke,140).
calories(cookie,132).
calories(naan,78).
calories(potato_soup,149).

meat(hamburger).
meat(chicken_soup).
legume(peanuts).
legume(beans).
legume(peas).
vegetable(carrot).
vegetable(potato_soup).
fruit(apple).
fruit(banana).
fruit(apple_pie).
fruit(orange_juice).
drink(water).
drink(coke).
drink(milk).
drink(orange_juice).

protein(X) :- meat(X). 
protein(X) :- legume(X).  
fruit_or_vegetable(X) :- fruit(X). 
fruit_or_vegetable(X) :- vegetable(X). 

tota_lSubFunction(Meal,Tmp,Cals) :-
	Meal=[H|T],
	calories(H,X),
	TmpNew is +(Tmp,X),
	tota_lSubFunction(T,TmpNew,Cals).
tota_lSubFunction([],Tmp,Cals) :-
	Cals = Tmp.
total(Meal,Cals) :- 
	tota_lSubFunction(Meal,0,Cals).

nutritious(M) :-
	total(M,Cals),
	400 =< Cals,
	Cals =< 600.
contains_protein(M) :-
	protein(X),
	member(X,M).
contains_fruit_or_vegetable(M) :-
	fruit_or_vegetable(X),
	member(X,M).
good_meal(M) :-
	contains_protein(M),
	contains_fruit_or_vegetable(M),
	nutritious(M).

can_add(X,Tmp) :-
	calories(X,X_Cals),
	total(Tmp,Cals),
	X_Cals + Cals =< 600.
	
good_meal_vegetarian_SubFunction(Tmp,Ans) :-
	\+ contains_protein(Tmp),
	can_add(X,Tmp),legume(X),\+ member(X,Tmp),
	append(Tmp,[X],TmpNew),
	good_meal_vegetarian_SubFunction(TmpNew,Ans).
good_meal_vegetarian_SubFunction(Tmp,Ans) :-
	\+ contains_fruit_or_vegetable(Tmp),
	can_add(X,Tmp),fruit_or_vegetable(X),\+ member(X,Tmp),
	append(Tmp,[X],TmpNew),
	good_meal_vegetarian_SubFunction(TmpNew,Ans).
good_meal_vegetarian_SubFunction(Tmp,Ans) :-
	\+ nutritious(Tmp),
	can_add(X,Tmp),\+ meat(X),\+ member(X,Tmp),
	append(Tmp,[X],TmpNew),
	good_meal_vegetarian_SubFunction(TmpNew,Ans).
good_meal_vegetarian_SubFunction(Tmp,Ans) :-
	good_meal(Tmp),
	Ans=Tmp.

good_meal_vegetarian :-
	good_meal_vegetarian_SubFunction([],Ans),
	total(Ans,Cals),
	write('An example of a good meal that is vegetarian:'),nl,
	write(Ans),nl,
	write('Caleries:'),write(Cals).
	
good_meal_non_vegetarian_SubFunction(Tmp,Ans) :-
	\+ contains_protein(Tmp),
	can_add(X,Tmp),meat(X),\+ member(X,Tmp),
	append(Tmp,[X],TmpNew),
	good_meal_non_vegetarian_SubFunction(TmpNew,Ans).
good_meal_non_vegetarian_SubFunction(Tmp,Ans) :-
	\+ contains_fruit_or_vegetable(Tmp),
	can_add(X,Tmp),fruit_or_vegetable(X),\+ member(X,Tmp),
	append(Tmp,[X],TmpNew),
	good_meal_non_vegetarian_SubFunction(TmpNew,Ans).
good_meal_non_vegetarian_SubFunction(Tmp,Ans) :-
	\+ nutritious(Tmp),
	can_add(X,Tmp),\+ member(X,Tmp),
	append(Tmp,[X],TmpNew),
	good_meal_non_vegetarian_SubFunction(TmpNew,Ans).
good_meal_non_vegetarian_SubFunction(Tmp,Ans) :-
	good_meal(Tmp),
	Ans=Tmp.

good_meal_non_vegetarian :-
	good_meal_non_vegetarian_SubFunction([],Ans),
	total(Ans,Cals),
	write('An example of a good meal that is non-vegetarian:'),nl,
	write(Ans),nl,
	write('Caleries:'),write(Cals).
