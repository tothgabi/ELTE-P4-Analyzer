package p4analyser.broker;

import java.io.File;

import org.codejargon.feather.Provides;

import p4analyser.ontology.providers.P4FileProvider.CoreP4File;
import p4analyser.ontology.providers.P4FileProvider.InputP4File;
import p4analyser.ontology.providers.P4FileProvider.V1ModelP4File;


class P4FileService {
    private File inputP4;
    private File coreP4;
    private File v1ModelP4;
    public P4FileService(String inputP4, String coreP4, String v1ModelP4){
        this.inputP4 = toFile(inputP4);
        this.coreP4 = toFile(coreP4);
        this.v1ModelP4 = toFile(v1ModelP4);
    }

    private File toFile(String fileName){
        File p4File = new File(fileName);
        if(!p4File.exists() || !p4File.isFile()){
            throw new IllegalArgumentException("Cannot find P4 file at location " + fileName);
        }
        return p4File;
    }

    @Provides @InputP4File
    public File inputP4(){ return inputP4; }

    @Provides @CoreP4File
    public File coreP4(){ return coreP4; }

    @Provides @V1ModelP4File
    public File v1ModelP4(){ return v1ModelP4; }
}