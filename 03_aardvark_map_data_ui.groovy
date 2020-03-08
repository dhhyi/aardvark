import groovy.swing.SwingBuilder
import groovy.beans.Bindable
import javax.swing.*
import static java.awt.EventQueue.invokeLater

def rules = new groovy.json.JsonSlurper().parseText(new File('rules.json').text)
def config = new groovy.json.JsonSlurper().parseText(new File('config.json').text)

GroovyShell shell = new GroovyShell()
def tools = shell.parse(new File('03_aardvark_map_data.groovy'))

new SwingBuilder().edt {
  frame(title: 'Java Frame', size: [1000, 600], locationRelativeTo: null, show: true, defaultCloseOperation: WindowConstants.EXIT_ON_CLOSE) {
    menuBar {
      menu(text: 'Rules') {
        menuItem(text: 'Add Rule', actionPerformed: { 
          rules.add([:])
          rulesTable.model.fireTableDataChanged()
          rulesTable.setRowSelectionInterval(rules.size() - 1, rules.size() - 1)
        })
        menuItem(text: 'Remove Rule', enabled: bind { rulesTable.selectedElement }, actionPerformed: { 
          rules.remove(rulesTable.selectedElement)
          rulesTable.model.fireTableDataChanged()
        })
        menuItem(text: 'Save Rules', actionPerformed: {
          new File('rules.json').withWriter { writer->
            writer.writeLine groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(rules))
          }
        })
      }
      menuItem(text: 'Generate Report', actionPerformed: { 
        tabs.selectedIndex = 1
        try {
          def report = tools.generateReport(rules, config).collect { it.join ';' }.join '\n'
          output.text = report
        } catch (err) {
          output.text= err.message
        }
      })
      menuItem(text: 'Write Report', actionPerformed: { 
        tabs.selectedIndex = 1
        new File('report.csv').withWriter { writer ->
          writer.writeLine output.text
        }
      })
    }  

    gridLayout(cols: 1, rows: 2)

    panel {
      borderLayout()
      scrollPane(constraints:CENTER) {
        table(id: 'rulesTable', selectionMode: ListSelectionModel.SINGLE_SELECTION) {
          tableModel(list:rules) {
            closureColumn(header:'match', read:{row -> return row.match})
            closureColumn(header:'group', read:{row -> return row.group})
            closureColumn(header:'task', read:{row -> return row.task})
            closureColumn(header:'activity', read:{row -> return row.activity})
            closureColumn(header:'comment', read:{row -> return row.comment})
          }
        }
        rulesTable.selectionModel.addListSelectionListener { 
          if (rulesTable.selectedElement != null)
            tabs.selectedIndex = 0
        }
      }
    }

    tabbedPane(id: 'tabs', tabLayoutPolicy:JTabbedPane.SCROLL_TAB_LAYOUT) {

      def selectedRule = { rules.find({ it.is(rulesTable.selectedElement) }) }

      panel(name: 'Rule Edit', visible: bind { rulesTable.selectedElement != null }) {
        gridLayout(cols: 2, rows: 5)
        
        label(text: 'Match: ', horizontalAlignment: SwingConstants.RIGHT)
        matchInput = textField(text: bind { rulesTable.selectedElement?.match })
        matchInput.addCaretListener { selectedRule()?.match = matchInput.text }

        def getGroups = { config.tasks.keySet() as List }
        label(text: 'Group: ', horizontalAlignment: SwingConstants.RIGHT)
        groupSelect = comboBox(items: getGroups(), selectedItem: bind { rulesTable.selectedElement?.group })
        groupSelect.addItemListener { selectedRule()?.group = groupSelect.selectedItem }
        
        label(text: 'Task: ', horizontalAlignment: SwingConstants.RIGHT)
        taskSelect = comboBox()
        def taskSelectUpdate = { 
          def list = config.tasks.find { key, value -> key =~ groupSelect.selectedItem }?.value ?: []
          taskSelect.model = new DefaultComboBoxModel(list.toArray())
          taskSelect.model.selectedItem = list.find { it =~ rulesTable.selectedElement?.task }
        }
        groupSelect.addItemListener taskSelectUpdate
        rulesTable.selectionModel.addListSelectionListener taskSelectUpdate
        taskSelect.addItemListener { 
          if (taskSelect.selectedItem != null)
            selectedRule().task = taskSelect.selectedItem
        }
        
        label(text: 'Activity: ', horizontalAlignment: SwingConstants.RIGHT)
        activitySelect = comboBox(items: config.activities, selectedItem: bind { rulesTable.selectedElement?.activity })
        activitySelect.addItemListener { selectedRule()?.activity = activitySelect.selectedItem }
        
        label(text: 'Comment: ', horizontalAlignment: SwingConstants.RIGHT)
        commentInput = textField(text: bind { rulesTable.selectedElement?.comment }, actionPerformed: { invokeLater { rulesTable.model.fireTableDataChanged() } })
        commentInput.addCaretListener { selectedRule()?.comment = commentInput.text }
      }

      panel(name: 'Output') {
        borderLayout()
        textArea(id: 'output', editable: true, alignmentY: SwingConstants.TOP)
      }
    }
  }
}
