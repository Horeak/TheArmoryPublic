package Core.Data;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.Save;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

class SaveDataThread extends Thread{
	@Override
	public void run(){
		super.run();

		Set<Field> fields = Startup.getReflection().getFieldsAnnotatedWith(Save.class);
		Set<Class<?>> classes = Startup.getReflection().getTypesAnnotatedWith(Save.class);

		DataHandler.initFields(fields);

		for(Class<?> c : classes){
			DataHandler.loadClass(c);

			for(Field fe : c.getFields()){
				if(Modifier.isStatic(fe.getModifiers())){
					DataHandler.objects.add(fe);

					Object t = DataHandler.getValue(fe);
					if(t != null){
						DataHandler.updateValue(fe, t);
					}
				}
			}
		}

		DataHandler.load_done = true;

		while(isAlive()){
			if(DataHandler.queueDataLoad.size() > 0){
				if(Startup.init){
					for(int i = 0; i < DataHandler.queueDataLoad.size(); i++){
						DataHandler.queueDataLoad.remove(i);
					}
				}
			}

			//TODO Try and find a more optimized way of checking and saving values in near real time
			DataHandler.check();

			try{
				Thread.sleep(100);
			}catch(InterruptedException e){
				Logging.exception(e);
			}
		}
	}
}