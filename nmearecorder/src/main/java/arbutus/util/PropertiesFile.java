package arbutus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesFile {
	private static final Logger _logger = Logger.getLogger(PropertiesFile.class);
	
	private static final Hashtable<String, PropertiesFile> _hashTableProperties = new Hashtable<String, PropertiesFile>();
	public static boolean DEBUG;

	private final Properties _prop = new java.util.Properties();;
	private String _filelocation;

	/**
	 * Cette méthode permet de récupérer ou créer l'objet PropertiesVM.
	 * @param absolutePathFile Chemin vers le fichier de propriétés.
	 * @return
	 */
	public static PropertiesFile getPropertiesVM(String absolutePathFile){
		PropertiesFile newPropertiesVM = null;
		synchronized (_hashTableProperties) {
			for(String fileAbsPath : _hashTableProperties.keySet()){
				if(fileAbsPath.equals(absolutePathFile)){
					return _hashTableProperties.get(fileAbsPath);
				}
			}

			newPropertiesVM = new PropertiesFile(absolutePathFile);
			_hashTableProperties.put(absolutePathFile, newPropertiesVM);
		}

		return newPropertiesVM;
	}

	private PropertiesFile(String fileLocation)
	{
		_filelocation = fileLocation;
		
		File file = new File(fileLocation);
		if(file.exists()) {
			try 
			{	
				FileInputStream fileInputStream = new FileInputStream(file);
				_prop.load(fileInputStream);
			} 
			catch (FileNotFoundException e) 
			{
				_logger.fatal(e.getMessage(), e);
			} 
			catch (IOException e) 
			{
				_logger.fatal(e.getMessage(), e);
			}
		}
		else {
			try {
				_prop.load(this.getClass().getClassLoader().getResourceAsStream(fileLocation));
			} catch (IOException e) {
				_logger.fatal(e.getMessage(), e);
			}
		}
	}

	/**
	 * Permet de récupérer la valeur d'une propriété. Cette méthode vérifie 
	 * la date de dernière mise à jour du fichier de propriété avant de retourner la valeur.
	 * @param key Nom de la propriété
	 * @return La valeur de la propriété
	 */
	public String getValue(String key)
	{
		if(_prop == null)
		{
			_logger.error("Aucune propriété n'a pu être chargée pour le fichier " + _filelocation);
			return "";
		}
		else
		{
			return _prop.getProperty(key, "").trim();
		}
	}

	public int getValueInt(String key, int defaultValue){
		if(_prop == null)
		{
			_logger.error("Aucune propriété n'a pu être chargée.");
			return defaultValue;
		}
		else
		{
			try{
				return Integer.parseInt(getValue(key));
			}
			catch(NumberFormatException e){
				_logger.error("Impossible de parser l'entier " + getValue(key) + " de l'attribut " + key + " pour le fichier " + _filelocation, e);
				return defaultValue;
			}
		}
	}

	public ArrayList<String> getKeys() {
		if(_prop != null)
		{
			ArrayList<String> keys = new ArrayList<String>(_prop.size());

			Enumeration<Object> enumKeys = _prop.keys();
			while(enumKeys.hasMoreElements()){
				String key = (String) enumKeys.nextElement();
				keys.add(key);
			}

			return keys;
		}
		else{
			return new ArrayList<String>(0);
		}
	}

	public boolean containsKey(String string) {
		return _prop.containsKey(string);
	}

	public float getValueFloat(String key, float defaultValue) {
		if(_prop == null)
		{
			_logger.error("Aucune propriété n'a pu être chargée.");
			return defaultValue;
		}
		else
		{
			try{
				return Float.parseFloat(getValue(key));
			}
			catch(NumberFormatException e){
				_logger.error("Impossible de parser le flottant " + getValue(key) + " de l'attribut " + key + " pour le fichier " + _filelocation, e);
				return defaultValue;
			}
		}
	}

}
