import java.io.IOException;
import java.util.Vector;
import java.util.Collections;
import java.util.StringTokenizer;


public class hcrminer extends Parent implements Interface
{
	public static int minsup;
	public static float minconf;
	public static String inputfile:
	public static String outputfile;
	public static int options;

	hcrminer() throw IOException
	{
		
	}

	public static void main (String[] args) throws IOException
	{
		minsup = args[1];
		minconf = args[2];
		inputfile = args[3];
		outputfile = args[4];
		options = args[5];
	}
}