module.exports = {
  parserPreset: {
    parserOpts: {
      // ALT: /^(SPEECHPLAN-[0-9]+|JDI)(?:\s*:?\s*)(.*)$/
      // NEU: Erlaubt ein optionales Emoji nach der Ticket-Nummer und vor dem eigentlichen Text.
      headerPattern: /^(SPEECHPLAN-[0-9]+|JDI)(?:\s*:?\s*)((?:\p{Emoji}\s)?)?(.*)$/u,
      headerCorrespondence: ['ticket', 'emoji', 'subject']
    }
  },
  rules: {
    // Diese Regeln bleiben gleich
    'header-min-length': [2, 'always', 5],
    'ticket-empty': [2, 'always']
  },
  plugins: [
    {
      rules: {
        'ticket-empty': (parsed) => {
          const { ticket } = parsed;
          if (!ticket) {
            return [false, 'Commit muss mit SPEECHPLAN-123 oder JDI beginnen'];
          }
          return [true];
        }
      }
    }
  ]
};
