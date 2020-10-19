package parser;

import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

public class AntlrP4 {


    // TODO formalize the analysis dependencies: https://github.com/j-easy/easy-flows
    public static void main(String[] args) {

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String remoteTraversalSourceName = args[2];

//        Graph graph = TinkerGraph.open();
//        GraphTraversalSource g = graph.traversal();
        GraphTraversalSource g = 
            AnonymousTraversalSource
                    .traversal()
                    .withRemote(DriverRemoteConnection.using(host, port, remoteTraversalSourceName));

//      TODO Normalisation introduces new nodes mid-tree. If we need 
//           node numbering to follow depth-first order, then node ids have to 
//           be reseted. This should be possible to do on client-side, 
//           but it is not very important so I left it for later.
//           Find the original in p4analysis/experts-everything-until-now/src/main/java/parser/GremlinUtils.java 
//           https://github.com/daniel-lukacs/p4analysis/commit/5852c715060ecfcb22397120f9a588a3a7721848

//        Normalisation.analyse(g);
//        GremlinUtils.resetNodeIds(g, Dom.SYN);

//      TODO not migrated yet to gremlin-server
//        ControlFlowAnalysis.analyse(g);
    }



// // stress test case generators 
//    public static void stressTest(int n) {
//        try {
//            PrintStream out = new PrintStream("/tmp/filename.txt");
//        
//            System.out.println("");
//            System.out.println("// 511");
//            f(511);        
//
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        System.out.println();  
//        System.exit(0);
//    }

//    public static void f(int n) {
//        if (n == 1) {
//            System.out.print("{ ipv4_lpm.apply(); }");
//        } else if (n > 1) {
//            System.out.print("{ ipv4_lpm.apply(); ");
//            f(n - 1);
//            System.out.print("}");
//        }
//    }
//
//    public static void g(int n, PrintStream out) {
//
//            if(n == 0){
//            out.print("ipv4_lpm.apply();");
//            }
//            else if(n > 0){
//            out.print("if(hdr.ipv4.isValid()){ipv4_lpm.apply();"); 
//            g(n - 1, out);
//            out.print("}else{ipv4_lpm.apply();"); 
//            g(n - 1, out);
//            out.print("}"); 
//            }
//            out.close();
//    }
//    public static void h(int n){
//        for (int i = 0; i < n; i++) {
//          System.out.print("{ ipv4_lpm.apply(); }");
//        }
//    }

}