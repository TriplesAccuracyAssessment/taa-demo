package kmi.taa_demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import kmi.taa.App;
import kmi.taa.core.FileHelper;

/**
 * Root resource (exposed at "verify" path)
 */
@Path("verify")
public class VerifyResource {
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     * @throws IOException 
     */    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getConfidence(@QueryParam("s") String subject,
    		@QueryParam("p") String predicate, @QueryParam("o") String object, 
    		@QueryParam("ptype") String ptype, @QueryParam("alpha") double alpha, 
    		@QueryParam("beta") double beta) throws IOException {
    	// create the source triple file
    	StringBuilder builder = new StringBuilder();
    	builder.append("1\t" + subject +"\t" + predicate + "\t" + object);
    	FileHelper.writeFile(builder.toString(), "./striples.txt", false);   
    	String pptysn = extractShortName(predicate);
    	String sKGendpoint = lookupEndpoint(subject);   
    	String result = appWrapper("./striples.txt", subject, pptysn, ptype, sKGendpoint, alpha, beta);
    	return result; 
    }
    
    public String appWrapper(String stripleFile, String s, String p, String pt, String endpoint, double alpha, double beta) throws IOException {
    	String[] args = new String[20];
    	args[0] = "--wklabels";
    	args[1] = "./resources/wikidata-properties-label.csv";
    	args[2] = "--pyPath";
    	args[3] = "/usr/local/bin/python2";
    	args[4] = "--mPath";
    	args[5] = "./python/";
    	args[6] = "-i";
    	args[7] = stripleFile;
    	args[8] = "--pptysn";
    	args[9] = p;
    	args[10] = "--pptytype";
    	args[11] = pt;
    	args[12] = "--workDir";
    	args[13] = "./results/";
    	args[14] = "--endpoint";
    	args[15] = endpoint;
    	args[16] = "--alpha";
    	args[17] = String.valueOf(alpha);
    	args[18] = "--beta";
    	args[19] = String.valueOf(beta);

    	try{
    		App.main(args);
    	}  catch(Exception e) {
    		e.printStackTrace();
    	}
    	String result = readResult(p);
    	return result;
    }
    
    public String extractShortName(String predicate) {
    	if(predicate.startsWith("<")) {
    		String[] tmp = predicate.split("/");
    		return tmp[tmp.length-1].substring(0, tmp[tmp.length-1].length()-1);
    	} else {
    		return predicate.split(":")[1];
    	}
    }
    
    public String lookupEndpoint (String subject) throws IOException {
    	String domain = subject.split("/")[2];
    	List<String> cont = Files.readAllLines(Paths.get("./resources/endpoint"), StandardCharsets.UTF_8);
    	for(String line:cont) {
    		String[] split = line.split("\t");
    		if(domain.contains(split[0]))
    			return split[1];
    	}		
    	return ""; 
    	
    }
    
    public String readResult(String p) throws IOException {
    	StringBuilder builder = new StringBuilder();
    	List<String> confidence = Files.readAllLines(Paths.get("./results/confidence_"+p+".txt"), StandardCharsets.UTF_8);
    	for(String line:confidence) {
    		builder.append(line + System.lineSeparator());    		
    	}
    	List<String> mtriples = Files.readAllLines(Paths.get("./results/matched_triples_"+p+".txt"), StandardCharsets.UTF_8);
    	for(String line:mtriples) {
    		builder.append(line + System.lineSeparator());    		
    	}
    	return builder.toString();
    }
}

