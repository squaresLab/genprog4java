package ylyu1.wean;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import clegoues.genprog4java.main.Configuration;
import clegoues.util.ConfigurationBuilder;
import static clegoues.util.ConfigurationBuilder.STRING;

public class YAMLDataProcessor extends AbstractDataProcessor{
	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();
	
	protected static String yamlOutputFile = ConfigurationBuilder.of( STRING )
			.withVarName("yamlOutputFile")
			.withDefault("out.yaml")
			.build();
	
	Writer writer;
	Yaml yaml;
	
	public YAMLDataProcessor()
	{
		try {
			writer = new BufferedWriter(new FileWriter(Configuration.workingDir + "/" + yamlOutputFile));
			yaml = new Yaml();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void dumpData(Map<String, Object> data)
	{
		yaml.dump(data, writer);
	}
	
	private void shutdown()
	{
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void storeError(String err) {
		shutdown();
	}

	@Override
	public void storeNormal() {
		shutdown();
	}
}
