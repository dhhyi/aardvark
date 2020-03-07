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

assert args && args[0], 'your companies BlueAnt URL must be provided as an argument'

io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup()
def driver = new org.openqa.selenium.chrome.ChromeDriver()
// io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup()
// def driver = new org.openqa.selenium.firefox.FirefoxDriver()

try {
    def wait = new WebDriverWait(driver, 60)
    driver.manage().window().maximize()

    driver.get(args[0])

    def zeiterfassungLink = wait.until(presenceOfElementLocated(
        By.xpath("(//a[@class='nav-entry'])[2]")
    ))
    sleep(2000)
    zeiterfassungLink.click()
    sleep(2000)

    driver.switchTo().frame(0);

    driver.findElement(By.cssSelector("input[name=datum]")).sendKeys(org.openqa.selenium.Keys.TAB)
    sleep 500
    driver.findElement(By.cssSelector("input[name=dauer]")).sendKeys(org.openqa.selenium.Keys.TAB)
    sleep 500
    def activeInput = driver.switchTo().activeElement()

    def config = [url: args[0]]
    def taskConfig = [:]

    def text
    do {

        activeInput.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN)
        sleep 500
        activeInput.sendKeys(org.openqa.selenium.Keys.ARROW_DOWN)
        sleep 500
        activeInput.sendKeys(org.openqa.selenium.Keys.ENTER)

        text = activeInput.getAttribute("value").replaceAll(/(_[0-9]*)?\ .*/, '').trim()

        sleep(2000)

        def taskSelect = driver.findElement(By.cssSelector("select[name=task]"))
        def taskSelectWrap = new org.openqa.selenium.support.ui.Select(taskSelect);

        def tasks = taskSelectWrap.getOptions()
            .findAll { it.getAttribute('disabled') != 'true' && !it.getAttribute('title').empty }
            .collect { it.getAttribute('title').replaceAll('\\u00a0', '').replaceAll(/^([0-9]+\.?)*/, '').replaceAll(/\(.*/, '').trim() }
        
        if (!text.empty) {
            taskConfig[text] = tasks
        }
    } while (text != '')

    config['tasks'] = taskConfig

    def activitySelect = driver.findElement(By.cssSelector("select[name=taetigkeit]"))
    def activitySelectWrap = new org.openqa.selenium.support.ui.Select(activitySelect);

    def activities = activitySelectWrap.getOptions()
      .findAll { !it.getAttribute('selected') }
      .collect { it.getAttribute('title') }

    config['activities'] = activities

    new File('config.json').withWriter { writer->
        writer.writeLine groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(config))
    }

} catch (Throwable err) {
    println "something went wrong: $err"
    throw err
} finally {
    driver.quit()
}
