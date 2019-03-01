package com.sncfc.crawler.db;

public abstract class BaseComplexProcess implements IComplexProcess{
	 public void begin(boolean reverse, int relation) {}

     public void end(boolean reverse) {}

     public boolean process(boolean reverse, int relation, String value) {
         return false;
     }

     public boolean goout() { return false; }

     public void nextRelation(boolean reverse, int relation) { }

     public void processEnd() { }
     public void processStart() { }
}
