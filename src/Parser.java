import java.util.*;

/* 		OO PARSER AND BYTE-CODE GENERATOR FOR TINY PL
 
Grammar for TinyPL (using EBNF notation) is as follows:

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

Sample Program: Factorial
 
int n, i, f;
n = 4;
i = 1;
f = 1;
while (i < n) {
  i = i + 1;
  f= f * i;
}
end

   Sample Program:  GCD
   
int x, y;
x = 121;
y = 132;
while (x != y) {
  if (x > y) 
       { x = x - y; }
  else { y = y - x; }
}
end

 */

public class Parser {
	public static HashMap<Character, Integer> id_hashmap = new HashMap<Character, Integer>();
	public static void main(String[] args)  {
		
		
		System.out.println("Enter program and terminate with 'end'!\n");
		Lexer.lex();
		Program p = new Program();
		Code.output();
	}
}

class Program {		/* program ->  decls stmts end */
	public static int index_of_map;
	public static int index;
	public static int count;
	public static int c;
	Decls decls;
	Stmts stmts;
	
	public Program() {
		
		decls = new Decls();
		stmts = new Stmts();	// look for multiple statements
		if(Lexer.nextToken == Token.KEY_END){
			Code.gen("return");
		}
	}
	 
}

class Decls {
	Idlist a;
	public Decls(){
		if(Lexer.nextToken == Token.KEY_INT) {
			a = new Idlist();
		}
	}
}

class Idlist { /* idlist  ->  id { , id } */
	
	public Idlist() {
		Lexer.lex();	//to get the next variable
		if(Lexer.nextToken == Token.ID) {
				Parser.id_hashmap.put(Lexer.ident, Program.index);
				Program.index++;
				Lexer.lex();	//to get the comma operator
			
			while(Lexer.nextToken == Token.COMMA) {
				Lexer.lex();
				if(Lexer.nextToken == Token.ID){
				
				Parser.id_hashmap.put(Lexer.ident, Program.index);
				Program.index++;
				Lexer.lex();	//to get the comma operator
				}
			}
			if(Lexer.nextToken == Token.SEMICOLON) {
				Lexer.lex();
			}
		}
	} 
}

class Stmt {
	Assign assign;
	Cond cond;
	Loop loop;
	public Stmt() {
//		Lexer.lex();
//		switch(Lexer.nextToken) {
//		case Token.ID:
//		case Token.ASSIGN_OP:
//			assign=new Assign();	
//			break;
//		case Token.KEY_IF:
//			cond= new Cond();
//			break;
//		case Token.KEY_WHILE:
//			loop=new Loop();
//			break;
//		}
		
		if(Lexer.nextToken == Token.ID){
			assign=new Assign();			
		}
		else if(Lexer.nextToken == Token.KEY_WHILE) {
			loop=new Loop();			
		}
		else if(Lexer.nextToken == Token.KEY_IF) {
			cond= new Cond();			
		}
	}
} 

class Stmts { //stmts   ->  stmt [ stmts ]
	 Stmts stmts;
	 Stmt stmt;
	 
	 public Stmts() {
		 stmt = new Stmt();
		 if((Lexer.nextToken != Token.RIGHT_BRACE) && Lexer.nextToken != Token.KEY_END){			 
			 			stmts = new Stmts();
			 		}
	}
}
	

class Assign {	//id = expr		eg. a = 3; a = b+4; a = b+c;
	Expr expr;
	Stmt stmt;
	public static char iden = ' ';

	 public Assign() { 
		 if(Lexer.nextToken == Token.ID){	//checks for a
			 iden = Lexer.ident;	
			 Lexer.lex(); //it checks for the assignment after the id

		 }
		 if(Lexer.nextToken == Token.ASSIGN_OP) { // = 8l
			 Lexer.lex();
			 expr = new Expr();
			 Lexer.lex();
			 Code.gen("istore_"+Parser.id_hashmap.get(iden));
		}
		 
		 if(Lexer.nextToken == Token.SEMICOLON){
			 Lexer.lex();
		 }
	 }
}

class Cond { //cond    ->  if '(' rexpr ')' cmpdstmt [ else cmpdstmt ]
	Rexpr rexpr;
	Cmpdstmt cmpdstmt1,cmpdstmt2;
	
	public Cond() {
		int if_goto_index;
		 if(Lexer.nextToken == Token.KEY_IF) {
			 Lexer.lex();
			 if(Lexer.nextToken ==  Token.LEFT_PAREN) {
				 rexpr = new Rexpr();
				 if(Lexer.nextToken ==  Token.RIGHT_PAREN) {
					 cmpdstmt1 = new Cmpdstmt();
					 //Lexer.lex();
					 if(Lexer.nextToken == Token.KEY_ELSE) {
						 Code.gen("goto ");
						 if_goto_index = Code.getCodeptr()-1;
						 Code.skip2();
						 Code.addEndIndex(rexpr.ifcmp_index);
						 cmpdstmt2 = new Cmpdstmt();
						 Code.addEndIndex(if_goto_index);		//added to set the goto variable for 'else' loop
						 //Lexer.lex();
					 }
					 else {
						 Code.addEndIndex(rexpr.ifcmp_index);	//added to set the goto for 'if' loop
					 }	
				 }
			 }
		 }
	}
}

class Loop { //loop    ->  while '(' rexp ')' cmpdstmt  
	 Rexpr rexpr;
	 Cmpdstmt cmpdstmt;
	 
	 public Loop() {
		 int while_start =  Code.getCodeptr(); ;	//identifier to store the starting of the while
		 if(Lexer.nextToken == Token.KEY_WHILE) {
			 Lexer.lex();
			 if(Lexer.nextToken == Token.LEFT_PAREN) {
				 rexpr = new Rexpr();
				 //Lexer.lex();	//check the right paren
				 if(Lexer.nextToken == Token.RIGHT_PAREN) {
					 cmpdstmt = new Cmpdstmt();
					 //Lexer.lex();
					 Code.gen("goto "+while_start);
					 Code.skip2();
					 Code.addEndIndex(rexpr.ifcmp_index);
				 }
			 }
		 }
	 }
}

class Cmpdstmt { //cmpdstmt->  '{' stmts '}'
	 Stmts stmts;
	 
	 public Cmpdstmt() {
		Lexer.lex();	// to get the left brace
		if(Lexer.nextToken == Token.LEFT_BRACE) {
			Lexer.lex();
			
			stmts = new Stmts() ;
			
		}
		if (Lexer.nextToken == Token.RIGHT_BRACE){
			Lexer.lex();
		}
	 }
}

class Rexpr { 	//rexp    ->  expr (< | > | =) expr
	Expr e1,e2;
	char op ;
	int lexerNextToken;	
	int ifcmp_index;
	
	public Rexpr() {
		Lexer.lex();
		e1 = new Expr();
		lexerNextToken = Lexer.nextToken;
		if(lexerNextToken == Token.LESSER_OP || lexerNextToken == Token.GREATER_OP || lexerNextToken == Token.ASSIGN_OP || lexerNextToken == Token.NOT_EQ)
		{
			op = Lexer.nextChar;
			Lexer.lex();
			e2 = new Expr();
			Code.gen(Code.opcode(op)); // fill with the pointer of right braces
			ifcmp_index = Code.getCodeptr()-1;	//to get the index of ifcmp statement
			Code.skip2();
		}
	}
	 
}

//expr    ->  term   [ (+ | -) expr ]
class Expr {  
	Term term;
	Expr expr;
	char op;
	public Expr() {
		term = new Term();
		if(Lexer.nextToken == Token.ADD_OP ||  Lexer.nextToken == Token.SUB_OP)
		{
			op =  Lexer.nextChar;
			Lexer.lex();
			expr = new Expr();
			Code.gen(Code.opcode(op));
		}
	}
	 
}

class Term {  
	Factor fact;
	Term term;
	char op;
	public Term() {
		//Lexer.lex();
		fact = new Factor();
		Lexer.lex();
		if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) {
			op = Lexer.nextChar;
			Lexer.lex();
			term = new Term();
			
			Code.gen(Code.opcode(op));
		}
	}
	 
}

class Factor { 
	Expr expr;
	
	public Factor() {
		if(Lexer.nextToken == Token.INT_LIT) {
			if(Parser.id_hashmap.containsKey(Assign.iden)){
				 Program.index_of_map = Parser.id_hashmap.get(Assign.iden);
				 if(Lexer.intValue >=-1 && Lexer.intValue<=5){
					 Code.gen("iconst_"+Lexer.intValue);
				 }
				 else if(Lexer.intValue>=6 && Lexer.intValue<=127){
					 Code.gen("bipush "+Lexer.intValue);
					 Code.skip1();
				 }
				 else{
					 Code.gen("sipush "+Lexer.intValue);
					 Code.skip2();
				 }
			 }
		}
		else if(Lexer.nextToken == Token.LEFT_PAREN) {
			Lexer.lex();	//left paren
			expr = new Expr();
		}
		else {	//lexer contains a id
			if(Parser.id_hashmap.containsKey(Lexer.ident)) {
				Code.gen("iload_"+Parser.id_hashmap.get(Lexer.ident));
			}
		}
	}
}

class Code {
	private static String[] code = new String[100];
	private static int codeptr = 0;
	
	public static void skip1() {
		codeptr+=1;
	}
	public static void skip2() {
		codeptr+=2;
	}
	public static void gen(String s) {
		code[codeptr] = s;
		codeptr++;
	}
	public static int getCodeptr() {
		return codeptr;
	}
	public static void addEndIndex(int ifcmp_index) {
		Code.code[ifcmp_index] = Code.code[ifcmp_index] + " " + codeptr;
	}
	
	public static String opcode(char op) {
		switch(op) {
		case '+' : return "iadd";
		case '-':  return "isub";
		case '*':  return "imul";
		case '/':  return "idiv";
		/* our Implementation */
		case '!':  return  "if_icmpeq";	
		case '>':  return  "if_icmple";	// http://en.wikipedia.org/wiki/Java_bytecode_instruction_listings
		case '<':  return  "if_icmpge";
		case '=':  return  "if_icmpne";		//need to check for two values
		/* == Implementation */
		default: return "";
		}
	}
	
	public static void output() {
		for (int i=0; i<codeptr; i++)
			if(code[i] != null) {
				System.out.println(i + ": " + code[i]);
			}
	}
	 
}