def rules = new groovy.json.JsonSlurper().parseText new File('rules.json').text
def config = new groovy.json.JsonSlurper().parseText(new File('config.json').text)

@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static com.xlson.groovycsv.CsvParser.parseCsv

def report = parseCsv(new File('report.csv').text, separator: ';', readFirstLine: true).collect { data ->
    def duration = Date.parse('HH:mm', data[2])
    duration.minutes = ((Math.floor((duration.minutes + 10) / 15) * 15).toInteger())

    if (duration.hours == 0 && duration.minutes < 15) {
        duration.minutes = 15
    }

    def rule = rules.find { data[1] =~ it.match }

    if (!rule) {
        println 'did not find mapping for "' + data[1] + '"'
        return
    }

    def groupEntry = config.tasks.find { it.key =~ rule.group }
    def task
    if (groupEntry) {
        task = groupEntry.value.find { it =~ rule.task }
    }

    if (!task || !groupEntry) {
        println 'did not find mapping for rule: '
        println groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(rule))
        return
    }

    return [data[0], duration.format('HH:mm'), rule.group, rule.task, rule.activity, rule.comment]
}

if (!report.findAll({ el -> el == null }).empty) {
    System.exit 1
}

new File('report.csv').withWriter { writer ->
    writer.writeLine report.collect { it.join(';') }.join('\n')
}