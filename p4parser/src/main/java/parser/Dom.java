package parser;

// TODO the semantic domain is redundant. the subdomains should have abstract edges (e.g. call, reference, name). it is not important what is the type of the elements these connect (control, parser, instance subdomains should be eliminated. or maybe a new subdomain should be introduced to denote these)  
// TODO remove subdomains from semantic analysis. each subdomain concerns the whole language so they are pointless.

// TODO make edge names into meaningful verbs

// TODO is there any way to reuse Java type system for domain checking?
public class Dom {
   public static final String SYN="syn";
   public static final String SEM="sem";
   public static final String CFG="cfg";
   public static final String CALL = "call";
   public static final String SITES = "sites";
   public static final String SYMBOL = "symbol";
   public static final String STRUCT = "struct";

   public static class Syn {
    public static class E {
        public static final String RULE="rule";
        public static final String ORD="ord";
    }
    public static class V {
        public static final String START="start";
        public static final String END="end";
        public static final String NODE_ID="nodeId";
        public static final String CLASS="class";
        public static final String VALUE="value";
    }
   }

   public static class Cfg {
    public static class E {
        public static final String ROLE="role";
        public static final String ORD="ord";

        public static class Role {
            public static final String LAB="lab";
            public static final String FLOW="flow";
            public static final String TRUE_FLOW="trueFlow";
            public static final String FALSE_FLOW="falseFlow";
            public static final String ASSOC="assoc";
            public static final String STATEMENT="statement";
            public static final String ENTRY="entry";
            public static final String RETURN="return";
        }
    }

    public static class V {
        public static final String TYPE="type";

        public static class Type {
            public static final String BLOCK="block"; // used in imperative algorithm
            public static final String ENTRY="entry";
            public static final String EXIT="exit";
        }
    }
   }

   public static class Sem  {
        public static final String DOMAIN="domain";
        public static final String ROLE="role";
        public static final String ORD="ord";

        public static class Domain {
            public static final String TOP ="top";
            public static final String PARSER ="parser";
            public static final String INSTANTIATION ="instantiation";
            public static final String CONTROL ="control";
        }

        public static class Role {
            public static class Top {
                public static final String PARSER ="parser";
                public static final String CONTROL ="control";
                public static final String INSTANTIATION ="instantiation";
            }

            public static class Control {
                public static final String NAME ="name";
				public static final String STATEMENT = "statement";
				public static final String BODY = "body";
				public static final String TRUE_BRANCH = "trueBranch";
				public static final String NEST = "nest";
				public static final String FALSE_BRANCH = "falseBranch";
				public static final String RETURN = "return";
				public static final String LAST = "last";
				public static final String FORK = "fork";
            }
            public static class Parser {
                public static final String NAME ="name";
                public static final String STATE ="state";
                public static final String START ="start";
                public static final String FINAL ="final";
                public static final String NEXT ="next";
                public static final String TRANSITION ="transition";
                public static final String HEAD ="head";
                public static final String CASE ="case";
                public static final String STATEMENT ="statement";
            }

            public static class Instantiation {
                public static final String ARGUMENT ="argument";
                public static final String NAME ="name";
                public static final String INVOKES ="invokes";
                public static final String TYPE ="type";
            }

        }
   }

   public static class Symbol  {
        public static final String ROLE="role";
        public static final String ORD="ord";

        public static class Role {
            public static final String HAS_TYPE ="hasType";
            public static final String DECLARES_NAME ="declaresName";
            public static final String SCOPES ="scopes";
        }
   }

   public static class Struct {
        public static final String ROLE="role";
        public static final String ORD="ord";

        public static class Role {

            public static final String TABLE = "table";
            public static final String ACTION = "action";
//            public static final String STRUCT_FIELD = "structField";
//            public static final String PARAMETER = "parameter";
        }
   }

   public static class Call {

        public static final String ROLE="role";
        public static final String ORD="ord";

        public static class Role {
            public static final String CALLS="calls";
        }
   }

   public static class Sites {

        public static final String ROLE="role";
        public static final String ORD="ord";

        public static class Role {
            public static final String CALLS="calls";
			public static final Object HAS_ARGUMENT = "hasArgument";
			public static final Object HAS_PARAMETER = "hasParameter";
			public static final Object INSTANTIATES = "instantiates";

        }
   }
}