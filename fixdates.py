import sys
import csv
from datetime import datetime
from dateutil import parser

# ['rdf-schema#label', 'birthDate', 'birthPlace_label', 'deathDate',
#   'field_label', 'genre_label', 'instrument_label', 'nationality_label',
#   'thumbnail', 'wikiPageID', 'description']
FIELDS = [1, 23, 25, 40, 50, 52, 62, 73, 124, 133, 137]

with open(sys.argv[1], newline='') as f:
    reader = csv.reader(f)
    writer = csv.writer(sys.stdout)
    for i, r in enumerate(reader):
        if i > 0 and i < 4:
            continue
        s = [r[x] for x in FIELDS]
        if i == 0:
            print(','.join(s))
        if i > 3:
            if '|' in s[1]:
                s[1] = s[1].strip('{}').split('|')[0]
            if s[1] != 'NULL':
                try:
                    s[1] = parser.parse(s[1])
                    s[1] = s[1].strftime('%Y-%m-%d 00:00:00')
                except:
                    s[1] = 'NULL'
            if '|' in s[3]:
                s[3] = s[3].strip('{}').split('|')[0]
            if s[3] != 'NULL':
                try:
                    s[3] = parser.parse(s[3])
                    s[3] = s[3].strftime('%Y-%m-%d 00:00:00')
                except:
                    s[3] = 'NULL'
            s[8] = s[8].replace(',', '%2C')
            if s[9] == 'NULL':
                s[9] = 0
            writer.writerow(s)
