import translations from './translations';

export default function customTranslate(template, replacements) {
  replacements = replacements || {};

  // Translate
  template = translations[template] || template;

  // Replace
  return template.replace(/{([^}]+)}/g, function(_, key) {
    var str = replacements[key];
    if (translations[str]) {
      return translations[str];
    }
    // Handle "bpmn:Task" -> "Task"
    if (str && str.includes(':')) {
       str = str.split(':')[1];
       if (translations[str]) {
         return translations[str];
       }
    }
    return str || '{' + key + '}';
  });
}
