import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.connectparticipant.model.Item;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args){
        Map<String,String> productToLink = new HashMap<>();
        //to use Selenium you need to download a webdriver so this is just the path to my webdriver
        System.setProperty("webdriver.chrome.driver",
                "C:\\Users\\corin\\chromedriver\\chromedriver.exe");
        ChromeDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, 30);
        driver.navigate().to("https://www.glossier.com/category/skincare");
        //I had to select based on an outer div because that was one of the only elements where all the products had
        //the same class name which made it easy to search
        List<WebElement> elements = driver.findElements(By.cssSelector("div[class='sc-GMQeP Lzopv']"));
        for(WebElement element : elements){
            //for each individual div that each product on the page has, we want to find the first attiribute with
            //tag p since that has the text of the product so we do a findElement() call on the div to search
            //only for elements within the div
            WebElement p = element.findElement(By.tagName("p"));
            System.out.println(p.getText());
            //to get the link we search for elements with tag 'a' within the div
            productToLink.put(p.getText(),element.findElement(By.tagName("a")).getAttribute("href"));
        }

        InstanceProfileCredentialsProvider credentials = InstanceProfileCredentialsProvider.createAsyncRefreshingProvider(true);
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .build();

        // the data hashmap represents the data schema of the database (since it's unstructured). The String key
        //is the key for the data field to put in the database, and the AttibuteValue is the value. The "S" means
        //that the value is a String, and there are other one/two letter keys for other data types. So here, this is
        //saying that the database will have a ProductName and a ProductLink and getting the data we parsed from the webpage
        //for those two fields
        for(String product : productToLink.keySet()){
            Map<String,AttributeValue> data = new HashMap<>();
            data.put("ProductName",new AttributeValue("\"S\": \"" + product + "\""));
            data.put("ProductLink",new AttributeValue("\"S\": \"" + productToLink.get(product) + "\""));
            client.putItem("GlossierProducts",data);
        }
}
}
