#include <core.p4>
#include <v1model.p4>

struct routing_metadata_t {
    bit<32> nhgroup;
}

header ethernet_t {
    bit<48> dstAddr;
    bit<48> srcAddr;
    bit<16> etherType;
}

header ipv4_t {
    bit<8>  versionIhl;
    bit<8>  diffserv;
    bit<16> totalLen;
    bit<16> identification;
    bit<16> fragOffset;
    bit<8>  ttl;
    bit<8>  protocol;
    bit<16> hdrChecksum;
    bit<32> srcAddr;
    bit<32> dstAddr;
}

struct metadata {
    routing_metadata_t routing_metadata;
}

struct headers { 
    ethernet_t ethernet;
    ipv4_t     ipv4;
}

parser ParserImpl(packet_in packet, out headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    state parse_ethernet {
        packet.extract(hdr.ethernet);
        @prb(75,25) {}
        transition select(hdr.ethernet.etherType) {
            16w0x800: parse_ipv4; 
            default: accept;
        }
    }
    state parse_ipv4 {
        packet.extract(hdr.ipv4);
        transition accept;
    }
    state start {
        transition parse_ethernet;
    }
}

control egress(inout headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    apply {
    }
}

control ingress(inout headers hdr, inout metadata meta, inout standard_metadata_t standard_metadata) {
    table ipv4_lpm {
        actions = {
            @prob(50) set_nhop;   
            @prob(50) _drop; 
        }
        key = {
            hdr.ipv4.dstAddr: lpm;
        }
        size = 1024;
    }
    action set_nhop(bit<32> nhgroup) {
        meta.routing_metadata.nhgroup = nhgroup;
        hdr.ipv4.ttl = hdr.ipv4.ttl + 8w0xff;
    }

    table nexthops {
        actions = {
            @prob(80) forward;
            @prob(20) _drop;
        }
        key = {
            meta.routing_metadata.nhgroup: exact;
        }
        size = 512;
    }
    action _drop() {
        mark_to_drop(standard_metadata);
    }
    action forward(bit<48> dmac_val, bit<48> smac_val, bit<9> port) {
        hdr.ethernet.dstAddr = dmac_val;
        standard_metadata.egress_port = port;
        hdr.ethernet.srcAddr = smac_val;
    }

//    apply {
//      if (hdr.ipv4.isValid()) {
//          ipv4_lpm.apply();
//          nexthops.apply();
//      }
//    }

    // syntax does not forbid redeclaration, and the spec does not mention it, but p4c forbids it.
    // not sure what happens when variable is declared in adjacent branches.
    apply {
      int x = 12;  
      const int y = 14;
      ipv4_lpm.apply(); // 1
      x = x + y;
            
      {ipv4_lpm.apply(); ipv4_lpm.apply();} // 2
      if (hdr.ipv4.isValid()) {
          ipv4_lpm.apply();
          ipv4_lpm.apply();
          ipv4_lpm.apply(); // 3
          { {} }
          nexthops.apply();
          nexthops.apply();
          nexthops.apply();
          nexthops.apply(); // 4
      } else {
          nexthops.apply();
          nexthops.apply();
          nexthops.apply();
          nexthops.apply();
          nexthops.apply(); // 5
      }
      { ipv4_lpm.apply();ipv4_lpm.apply();ipv4_lpm.apply();ipv4_lpm.apply();ipv4_lpm.apply();ipv4_lpm.apply();} // 6

      nexthops.apply();
      nexthops.apply();
      nexthops.apply();
      nexthops.apply();
      nexthops.apply();
      nexthops.apply();
      nexthops.apply(); // 7
    }
}

control DeparserImpl(packet_out packet, in headers hdr) {
    apply {
        packet.emit(hdr.ethernet);
        packet.emit(hdr.ipv4);
    }
}

control verifyChecksum(inout headers hdr, inout metadata meta) {
    apply {
        
    }
}

control computeChecksum(inout headers hdr, inout metadata meta) {
    apply {
        
    }
}

V1Switch(ParserImpl(), verifyChecksum(), ingress(), egress(), computeChecksum(), DeparserImpl()) main;
