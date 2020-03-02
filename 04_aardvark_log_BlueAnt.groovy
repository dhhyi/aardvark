@Grapes([
    @Grab('org.seleniumhq.selenium:selenium-chrome-driver:3.141.59'),
    @Grab('org.seleniumhq.selenium:selenium-firefox-driver:3.141.59'),
    @Grab('org.seleniumhq.selenium:selenium-support:3.141.59'),
    @Grab('io.github.bonigarcia:webdrivermanager:3.8.1'),
    @Grab('org.slf4j:slf4j-simple:1.7.30')
])
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated

@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static com.xlson.groovycsv.CsvParser.parseCsv

def config = new groovy.json.JsonSlurper().parseText(new File('config.json').text)

io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup()
def driver = new org.openqa.selenium.chrome.ChromeDriver()
// io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup()
// def driver = new org.openqa.selenium.firefox.FirefoxDriver()

try {
    def wait = new WebDriverWait(driver, 60)

    driver.manage().window().maximize()

    driver.get(config.url)

    def zeiterfassungLink = wait.until(presenceOfElementLocated(
        By.xpath("(//a[@class='nav-entry'])[2]")
    ))
    sleep(2000)
    zeiterfassungLink.click()
    sleep(2000)

    driver.switchTo().frame(0);

    parseCsv(new File('report.csv').text, separator: ';', readFirstLine: true).each { data ->

        //DATE
        def date = driver.findElement(By.cssSelector("input[name=datum]"))
        if (date.getAttribute("value") != data[0]) {
            date.clear()
            sleep 1000
            date.sendKeys(data[0]);
            sleep 1000
            driver.findElement(By.xpath("//button[text()='schließen']")).click()
            sleep 1000
        }

        // DURATION
        def duration = driver.findElement(By.cssSelector("input[name=dauer]"))
        duration.sendKeys(data[1])
        sleep 500
        duration.sendKeys(org.openqa.selenium.Keys.TAB)

        // GROUP
        def activeInput = driver.switchTo().activeElement()
        do {
            activeInput.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN)
            sleep 200
        } while (!(activeInput.getAttribute("value") =~ data[2]))
        activeInput.sendKeys(org.openqa.selenium.Keys.ENTER)
        sleep 500
        activeInput.sendKeys(org.openqa.selenium.Keys.TAB)
        sleep 500

        // TASK
        activeInput = driver.switchTo().activeElement()
        do {
            activeInput.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN)
            sleep 200
        } while (!(activeInput.getAttribute("value") =~ data[3]))
        activeInput.sendKeys(org.openqa.selenium.Keys.ENTER)
        sleep 500

        do {
            activeInput.sendKeys(org.openqa.selenium.Keys.TAB)
            sleep 500
            activeInput = driver.switchTo().activeElement()
        } while(activeInput.getAttribute("title") != 'Please select')
        
        // ACTIVITY
        activeInput = driver.switchTo().activeElement()
        do {
            activeInput.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN)
            sleep 200
        } while (!(activeInput.getAttribute("value") =~ data[4]))
        activeInput.sendKeys(org.openqa.selenium.Keys.TAB)
        sleep 500

        // COMMENT
        activeInput = driver.switchTo().activeElement()
        activeInput.sendKeys data[5]
        sleep 500

        driver.findElement(By.cssSelector("button[name=speichern]")).click()
        sleep 2000

    }
    sleep 5000

} catch (Throwable err) {
    println "something went wrong: $err"
    throw err
} finally {
    driver.quit()
}
