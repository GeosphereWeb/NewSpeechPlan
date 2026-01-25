module.exports = {
  parserPreset: {
    parserOpts: {
      // Erlaubt Formate wie "SPECHPLAN-123: text" oder "JDI: text"
      headerPattern: /^(SPECHPLAN-[0-9]+|JDI)(?:\s*:?\s*)(.*)$/,
      headerCorrespondence: ['ticket', 'subject']
    }
  },
  rules: {
    'header-min-length': [2, 'always', 5],
    'ticket-empty': [2, 'always']
  },
  plugins: [
    {
      rules: {
        'ticket-empty': (parsed) => {
          const { ticket } = parsed;
          if (!ticket) {
            return [false, 'Commit muss mit SPECHPLAN-123 oder JDI beginnen'];
          }
          return [true];
        }
      }
    }
  ]
};
