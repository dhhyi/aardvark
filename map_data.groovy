@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static com.xlson.groovycsv.CsvParser.parseCsv

def generateReport(rules, config) {
    parseCsv(new File('report.csv').text, separator: ';', readFirstLine: true).collect { data ->
        def duration = Date.parse('HH:mm', data[2])
        duration.minutes = ((Math.floor((duration.minutes + 10) / 15) * 15).toInteger())

        if (duration.hours == 0 && duration.minutes < 15) {
            duration.minutes = 15
        }

        def rule = rules.find { data[1] =~ it.match }

        if (!rule) {
            throw new Exception('did not find mapping for "' + data[1] + '"')
        }

        def groupEntry = config.tasks.find { it.key =~ rule.group }
        def task
        if (groupEntry) {
            task = groupEntry.value.find { it =~ rule.task }
        }

        if (!task || !groupEntry) {
            throw new Exception('did not find mapping for rule: ' + groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(rule)))
        }

        def comment = rule.comment.contains('$') ? data[1].replaceAll(rule.match, rule.comment) : rule.comment

        return [data[0], data[1], duration.format('HH:mm'), rule.group, rule.task, rule.activity, comment]
    }
}

def rules = new groovy.json.JsonSlurper().parseText new File('rules.json').text
def config = new groovy.json.JsonSlurper().parseText(new File('config.json').text)

try {
    def report = generateReport(rules, config)

    new File('report.csv').withWriter { writer ->
        writer.writeLine report.collect { it.join(';') }.join('\n')
    }
} catch (err) {
    println err.message
}
