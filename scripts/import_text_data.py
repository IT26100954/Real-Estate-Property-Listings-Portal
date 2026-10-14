"""Create a private SQL import script from the OLD portal's data; never runs SQL itself."""
import argparse
from pathlib import Path
FIELDS={
 'users':'username password email phone userType',
 'admins':'username password email adminLevel',
 'agents':'fullName email phone licenseNo city experienceYears agentType commissionRate',
 'properties':'title propertyType listingType city address price bedrooms bathrooms areaSqft ownerId agentId status extraValue',
 'appointments':'propertyId buyerId agentId viewDate viewTime viewType status note',
 'offers':'propertyId buyerId offerType offerAmount durationMonths offerDate status',
 'reviews':'reviewerId targetId reviewType rating comment reviewDate',
 'activity_log':'actorId action dateTime'}
def decode(s):
 return s.replace('%0D','\r').replace('%0A','\n').replace('%7C','|').replace('%25','%')
def literal(s):
 return "'"+s.replace('\\','\\\\').replace("'","''")+"'"
def main():
 p=argparse.ArgumentParser();p.add_argument('--source',required=True);p.add_argument('--output',default='database/imported_private_data.sql');a=p.parse_args()
 lines=['-- PRIVATE DATA: do not commit or share. Import into an EMPTY schema; skip demo data.', 'START TRANSACTION;']
 for key,fields in FIELDS.items():
  path=Path(a.source)/(key+'.txt')
  if not path.is_file():raise SystemExit(f'Missing required file: {path}')
  names=['logId' if key=='activity_log' else 'id']+fields.split()
  for n,line in enumerate(path.read_text(encoding='utf-8-sig').splitlines(),1):
   if not line.strip():continue
   values=[decode(v) for v in line.split('|')]
   if len(values)!=len(names):raise SystemExit(f'{path.name}:{n}: expected {len(names)} columns')
   if key in ['users','admins'] and not values[names.index('password')].startswith('pbkdf2$'):
    raise SystemExit(f'{path.name}:{n}: expected an existing PBKDF2 password hash')
   lines.append('INSERT INTO `'+key+'` ('+','.join('`'+f+'`' for f in names)+') VALUES ('+','.join(literal(v) for v in values)+');')
 lines.append('COMMIT;');output=Path(a.output);output.parent.mkdir(parents=True,exist_ok=True);output.write_text('\n'.join(lines)+'\n',encoding='utf-8')
 print('Private SQL script written. Import into an empty database using MySQL Workbench; keep out of Git.')
if __name__=='__main__':main()
