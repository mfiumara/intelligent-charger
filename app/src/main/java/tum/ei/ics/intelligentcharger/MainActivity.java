package tum.ei.ics.intelligentcharger;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tum.ei.ics.intelligentcharger.adapter.CycleAdapter;
import tum.ei.ics.intelligentcharger.entity.Cycle;
import tum.ei.ics.intelligentcharger.entity.Event;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.updateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateList() {
        ListView lv = (ListView) findViewById(R.id.lvList);
        final CycleAdapter cycleAdapter = new CycleAdapter(this);
        List<Cycle> cycles = Cycle.listAll(Cycle.class);
        cycleAdapter.setData(cycles);
        lv.setAdapter(cycleAdapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: Set click method for cycle items so the user can erase useless cycles.
            }
        });
    }

    public void updateList(View view) { updateList(); } // I know it's ugly...

    public void fitPredictor(View view) {
        long startTime = System.nanoTime();

        Attribute plugTimeAttribute= new Attribute("Plugtime");
        Attribute unplugTimeAttribute = new Attribute("UnplugTime");

        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(plugTimeAttribute);
        attributeList.add(unplugTimeAttribute);

        // Create training set from list of Cycles
        List<Cycle> cycles = Cycle.listAll(Cycle.class);
        Integer N = cycles.size();
        Instances trainingSet = new Instances("Rel", attributeList, N);
        trainingSet.setClassIndex(1);

        // Add all cycles to training set
        for(Cycle cycle : cycles) {
            Event plugEvent = cycle.getPluginEvent();
            Event unplugEvent = cycle.getPlugoutEvent();

            String plugDateString = plugEvent.getDatetime();
            String unplugDatestring = unplugEvent.getDatetime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date plugDate = new Date();
            Date unplugDate = new Date();
            try {
                plugDate = sdf.parse(plugDateString);
                unplugDate = sdf.parse(unplugDatestring);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar plugCal = Calendar.getInstance();
            Calendar unplugCal = Calendar.getInstance();
            plugCal.setTime(plugDate);
            unplugCal.setTime(unplugDate);

            float plugTime = (float) (plugCal.get(Calendar.HOUR_OF_DAY) +
                    plugCal.get(Calendar.MINUTE) /  60.0 + plugCal.get(Calendar.SECOND) / 3600.0);
            float unplugTime = (float) (unplugCal.get(Calendar.HOUR_OF_DAY) +
                    unplugCal.get(Calendar.MINUTE) /  60.0 + unplugCal.get(Calendar.SECOND) / 3600.0);

            Instance instance = new DenseInstance(2);
            instance.setValue((Attribute) attributeList.get(0), plugTime);
            instance.setValue((Attribute) attributeList.get(1), unplugTime);

            // add the instance
            trainingSet.add(instance);
        }

        // Create the model
        Classifier randomForest = (Classifier) new RandomForest();
        Classifier linearRegression = (Classifier) new LinearRegression();

        try {
            randomForest.buildClassifier(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            linearRegression.buildClassifier(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create a testing set.
        Instance testInstance = new DenseInstance(2);
        testInstance.setValue((Attribute) attributeList.get(0), 12.1);
        testInstance.setDataset(trainingSet);

        try {
            double[] output1 = randomForest.distributionForInstance(testInstance);
            double[] output2 = linearRegression.distributionForInstance(testInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        float duration = (float) ((endTime - startTime) / 1000000.0);
        Log.v(TAG, "Time taken to fit: " + Float.toString(duration) + " ms.");
    }

    public void testWeka(View view) {
        /*
        Integer N = 15;

        Attribute plugTimeAttribute= new Attribute("plugtime");
        Attribute unplugTimeAttribute = new Attribute("unplugTime");

        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(plugTimeAttribute);
        attributeList.add(unplugTimeAttribute);

        Instances instances = new Instances("Rel", attributeList, N);
        instances.setClassIndex(1);

        for (Integer i = 0; i < N; i++) {
            Instance instance = new DenseInstance(2);
            instance.setValue(plugTimeAttribute, i);
            instance.setValue(unplugTimeAttribute, i + 5);
            instances.add(instance);
        }
*/
        // Declare two numeric attributes
        Attribute Attribute1 = new Attribute("firstNumeric");
        Attribute Attribute2 = new Attribute("secondNumeric");

        // Declare a nominal attribute along with its values
        FastVector fvNominalVal = new FastVector(3);
        fvNominalVal.addElement("blue");
        fvNominalVal.addElement("gray");
        fvNominalVal.addElement("black");
        Attribute Attribute3 = new Attribute("aNominal", fvNominalVal);

        // Declare the class attribute along with its values
        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("positive");
        fvClassVal.addElement("negative");
        Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

        // Declare the feature vector
        FastVector fvWekaAttributes = new FastVector(4);
        fvWekaAttributes.addElement(Attribute1);
        fvWekaAttributes.addElement(Attribute2);
        fvWekaAttributes.addElement(Attribute3);
        fvWekaAttributes.addElement(ClassAttribute);

        Instances trainingSet = new Instances("Rel", fvWekaAttributes, 10);
        trainingSet.setClassIndex(3);
        // Create the instance
        Instance iExample = new DenseInstance(4);
        iExample.setValue((Attribute)fvWekaAttributes.elementAt(0), 1.0);
        iExample.setValue((Attribute)fvWekaAttributes.elementAt(1), 0.5);
        iExample.setValue((Attribute)fvWekaAttributes.elementAt(2), "gray");
        iExample.setValue((Attribute)fvWekaAttributes.elementAt(3), "positive");

        // add the instance
        trainingSet.add(iExample);

        // Create the model
        Classifier linearRegression = (Classifier) new LinearRegression();
        Classifier cModel = (Classifier)new NaiveBayes();
        try {
            cModel.buildClassifier(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            linearRegression.buildClassifier(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Integer foo = 1;
    }
}
