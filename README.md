Object-Oriented-Parser-
=======================

####Grammar for TinyPL (using EBNF notation) is as follows:

 program ->  decls stmts end    
 decls   ->  int idlist ;	
 idlist  ->  id { , id } 		
 stmts   ->  stmt [ stmts ]	
 cmpdstmt->  '{' stmts '}'	
 stmt    ->  assign | cond | loop	
 assign  ->  id = expr ;	
 cond    ->  if '(' rexp ')' cmpdstmt [ else cmpdstmt ] 	
 loop    ->  while '(' rexp ')' cmpdstmt  	
 rexp    ->  expr (< | > | =) expr	
 expr    ->  term   [ (+ | -) expr ]	
 term    ->  factor [ (* | /) term ]	
 factor  ->  int_lit | id | '(' expr ')'	
 
Lexical:   id is a single character; 	
	      int_lit is an unsigned integer;	
		 equality operator is =, not ==	
		 
		 
