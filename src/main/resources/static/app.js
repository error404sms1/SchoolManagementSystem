/* SMS Classic Large-Text Dashboard — same backend functionality, redesigned visual interface.
 * This is a presentation layer for the existing Sms* backend, not a security replacement.
 * No passwords/tokens are stored in localStorage, no external CDN dependencies.
 * All user and database text is rendered with textContent (never raw HTML).
 */
'use strict';
const $ = id => document.getElementById(id);
const F = (name,label,type='text',required=false,extras={}) => ({name,label,type,required,...extras});
const op = (title,method,url,fields=[],extras={}) => ({title,method,url,fields,...extras});
const streams = ['SCIENCE','TECHNOLOGY','COMMERCE','ARTS'];
const PAGE_SIZE = 12;
const state = {csrf:null,user:null,module:'overview',data:null,tableRows:[],page:0,query:'',lastAction:null,modalAction:null,loading:false};
const errorDefault='The request could not be completed. Check your Spring Boot console.';
const GROUPS = {
 overview: {
  title:'Overview',description:'Dashboard, student numbers, admissions and property counts.',access:'admin',
  actions:[op('Dashboard and counts','GET','/api/admin/dashboard'),
   
   op('Active students in all grades','GET','/api/admin/enrollments'),
   op('Teacher assignments and scopes','GET','/api/admin/scopes')]
 },
 accounts: {
  title:'Accounts & permissions',description:'Only an administrator can register teachers or another administrator.',access:'admin',
  actions:[op('List users (no passwords)','GET','/api/admin/accounts'),
   op('Register teacher or additional administrator','POST','/api/admin/accounts',[
    F('username','Login username','text',true),F('password','Temporary login password (12+ characters)','password',true),
    F('role','Account role','select',true,{options:['TEACHER','ADMIN']}),F('teacherCode','Existing teacher ID (required for TEACHER)')]),
   op('Assign teacher to a grade and subject','POST','/api/admin/scopes',[
    F('teacherCode','Teacher ID','text',true),F('gradeNumber','Grade number','number',true,{min:1,max:13}),F('subjectId','Subject ID','text',true)]),
   op('Remove teacher grade/subject permission','DELETE','/api/admin/scopes',[
    F('teacherCode','Teacher ID','text',true),F('grade','Grade number','number',true,{min:1,max:13}),F('subjectId','Subject ID','text',true)],{query:['teacherCode','grade','subjectId']}),
   op('Disable or enable an account','PUT','/api/admin/accounts/:username/active',[
    F('username','Username','text',true),F('active','Account enabled','select',true,{options:['true','false']})],{query:['active']}),
   op('Reset an account password','PUT','/api/admin/accounts/:username/password',[
    F('username','Username','text',true),F('password','New password (12+ characters)','password',true)]),
   op('View teacher scopes','GET','/api/admin/scopes')]
 },
 students: {
  title:'Student registration',description:'Register a student using the existing STUDENT table, then set a grade enrollment.',access:'admin',
  actions:[op('Register a new student','POST','/api/admin/students',[
    F('studentId','Student ID','text',true),F('studentName','Student name','text',true),
    F('birthday','Birth date','date',true),F('studentAddress','Address','textarea',true)]),
   op('Enroll student in a grade','POST','/api/admin/enrollments',[
    F('studentId','Existing student ID','text',true),F('gradeNumber','Grade number','number',true,{min:1,max:10}),
    F('seatType','Seat type','select',true,{options:['ORDINARY','SCHOLARSHIP']}),
    F('active','Still enrolled','select',true,{options:['true','false']})]),
   op('List grade enrollments','GET','/api/admin/enrollments',[
    F('grade','Grade (leave empty for all)','number',false,{min:1,max:13})],{query:['grade']}),
   op('Student full profile by ID','GET','/api/admin/students/:studentId/profile',[
    F('studentId','Student ID','text',true)])]
 },
 academic: {
  title:'Marks & academic records',description:'Administrators can view subject marks by student ID and subject ID.',access:'admin',
  actions:[op('Student marks: student ID + subject ID','GET','/api/admin/marks',[
    F('studentId','Student ID','text',true),F('subjectId','Subject ID','text',true)],{query:['studentId','subjectId']}),
   op('Full student profile by ID','GET','/api/admin/students/:studentId/profile',[
    F('studentId','Student ID','text',true)]),
   op('List current subjects and grades assigned to teachers','GET','/api/admin/scopes')]
 },
 properties: {
  title:'Physical properties',description:'Add/update property counts and conditions, or find the total number of units.',access:'admin',
  actions:[op('Add or update a property','POST','/api/admin/properties',[
    F('propertyId','Property ID','text',true),F('propertyName','Property name','text',true),
    F('quantity','Quantity','number',true,{min:0}),F('conditionNote','Condition / location details','textarea')]),
   op('List all property records','GET','/api/admin/properties'),
   op('Get total physical property quantity','GET','/api/admin/properties/count'),
   op('Delete property record','DELETE','/api/admin/properties/:propertyId',[
    F('propertyId','Property ID','text',true)])]
 },
 grade1: {
  title:'Grade 1 admissions',description:'Every applicant remains recorded. First-come-first-served selection shows the first N applications.',access:'admin',
  actions:[op('Set Grade 1 student limit','PUT','/api/admin/admissions/grade1/capacity',[
    F('limit','Capacity (e.g. 100)','number',true,{min:0})]),
   op('Read Grade 1 capacity','GET','/api/admin/admissions/grade1/capacity'),
   op('Record a Grade 1 application','POST','/api/admin/sms-admissions/grade1',[
    F('admissionId','Admission ID','text',true),F('studentName','Student name','text',true),
    F('birthDate','Birth date','date',true),F('assignDate','Application date','date'),
    F('distance','Distance from school (km)','number',false,{min:0,step:'0.01'}),
    F('marks','Marks','number',false,{min:0}),
    F('parentInSchool','Parent studied here','checkbox'),F('siblingInSchool','Sibling studies here','checkbox'),
    F('parentTeaPrin','Parent teacher/principal','checkbox'),F('parentOfficer','Parent officer','checkbox'),
    F('parentAbroad','Parent works abroad','checkbox')]),
   op('List ALL Grade 1 applicants','GET','/api/admin/sms-admissions/grade1'),
   op('Select first N Grade 1 applicants','POST','/api/admin/admissions/grade1/select'),
   op('Show only selected applicants','GET','/api/admin/admissions/grade1/selected'),
   op('Check one applicant’s result','GET','/api/admin/sms-admissions/grade1/:admissionId/result',[
    F('admissionId','Admission ID','text',true)]),
   op('Edit a Grade 1 application','PUT','/api/admin/sms-admissions/grade1/:admissionId',[
    F('admissionId','Admission ID','text',true),F('studentName','Student name','text',true),F('birthDate','Birth date','date',true),
    F('assignDate','Application date','date'),F('distance','Distance (km)','number',false,{min:0,step:'0.01'}),
    F('marks','Marks','number',false,{min:0}),F('parentInSchool','Parent studied here','checkbox'),
    F('siblingInSchool','Sibling studies here','checkbox'),F('parentTeaPrin','Parent teacher/principal','checkbox'),
    F('parentOfficer','Parent officer','checkbox'),F('parentAbroad','Parent works abroad','checkbox')]),
   op('Delete an application','DELETE','/api/admin/sms-admissions/grade1/:admissionId',[
    F('admissionId','Admission ID','text',true)])]
 },
 intermediate: {
  title:'Intermediate admissions (Grades 2–10)',description:'Vacancy-based admission with relocation priority and extra Grade 6 scholarship seats.',access:'admin',
  actions:[op('Record an intermediate application','POST','/api/admin/admissions/intermediate',[
    F('studentId','Applicant ID','text',true),F('gradeNumber','Requested grade','number',true,{min:2,max:10}),
    F('scholarshipMark','Grade 5 scholarship marks (Grade 6 only)','number',false,{min:0}),
    F('parentTransferKm','Parent workplace transfer distance (km)','number',false,{min:0,step:'0.01'}),
    F('homeMoveKm','Home relocation distance (km)','number',false,{min:0,step:'0.01'})]),
   op('Read Grade 6 scholarship settings','GET','/api/admin/admissions/intermediate/grade6-settings'),
   op('Update Grade 6 extra seats and school cutoff','PUT','/api/admin/admissions/intermediate/grade6-settings',[
    F('additionalSeats','Additional scholarship seats','number',true,{min:0}),F('scholarshipCutoff','School scholarship cutoff','number',true,{min:0})]),
   op('List applicants in a grade','GET','/api/admin/admissions/intermediate',[
    F('grade','Grade number (optional)','number',false,{min:2,max:10})],{query:['grade']}),
   op('Select applicants for a grade','POST','/api/admin/admissions/intermediate/select/:grade',[
    F('grade','Grade number','number',true,{min:2,max:10})])]
 },
 al: {
  title:'A/L stream admissions',description:'Separate seat limits for each stream, with existing-school students selected first.',access:'admin',
  actions:[op('Record an A/L application','POST','/api/admin/admissions/al',[
    F('studentId','Applicant ID','text',true),F('streamName','Stream','select',true,{options:streams})]),
   op('Set one stream capacity','PUT','/api/admin/admissions/al/capacity/:streamName',[
    F('streamName','Stream','select',true,{options:streams}),F('limit','Seats for this stream','number',true,{min:0})]),
   op('Set the SAME capacity in all four streams','MULTI','/api/admin/admissions/al/capacity/:streamName',[
    F('limit','Seats per stream (e.g. 30)','number',true,{min:0})]),
   op('See limits for each A/L stream','GET','/api/admin/admissions/al/capacity'),
   op('List ALL A/L applicants','GET','/api/admin/admissions/al'),
   op('Select students for a stream','POST','/api/admin/admissions/al/select/:streamName',[
    F('streamName','Stream','select',true,{options:streams})])]
 },
 assignmentsAdmin: {
  title:'Teacher assignments',description:'Administrators can inspect and download teacher-uploaded assignments.',access:'admin',
  actions:[op('View all uploaded assignments','GET','/api/admin/assignments'),
   op('Download assignment by ID','DOWNLOAD','/api/admin/assignments/:id/download',[
    F('id','Assignment ID','number',true,{min:1})])]
 },
 teacherHome: {
  title:'Assigned teacher work',description:'View your assigned grades and subjects. Administrator accounts can also use teaching functions.',access:'teacher',
  actions:[op('My assigned grade/subject scopes','GET','/api/teacher/my-scopes'),
   op('Students in my grade and subject','GET','/api/teacher/students',[
    F('grade','Grade','number',true,{min:1,max:13}),F('subjectId','Subject ID','text',true)],{query:['grade','subjectId']})]
 },
 teacherAttendance: {
  title:'Student attendance',description:'Attendance can only be recorded for students and subjects within your allowed scope.',access:'teacher',
  actions:[op('Record attendance','POST','/api/teacher/attendance',[
    F('studentId','Student ID','text',true),F('date','Attendance date','date',true),
    F('subjectId','Subject ID','text',true),F('present','Attendance status','select',true,{options:['true','false']})]),
   op('View attendance in my class','GET','/api/teacher/attendance',[
    F('grade','Grade','number',true,{min:1,max:13}),F('subjectId','Subject ID','text',true)],{query:['grade','subjectId']})]
 },
 teacherMarks: {
  title:'Subject marks',description:'Teachers can enter or view marks only for authorized students, grades and subjects.',access:'teacher',
  actions:[op('Add or update marks','POST','/api/teacher/marks',[
    F('studentId','Student ID','text',true),F('subjectId','Subject ID','text',true),
    F('termName','Term (e.g. Term 1)','text',true),F('mark','Marks 0–100','number',true,{min:0,max:100,step:'0.01'})]),
   op('Get a student’s marks in my subject','GET','/api/teacher/marks',[
    F('studentId','Student ID','text',true),F('subjectId','Subject ID','text',true)],{query:['studentId','subjectId']})]
 },
 teacherProfile: {
  title:'Student profile updates',description:'Teachers may add achievements and behavior notes for authorized students.',access:'teacher',
  actions:[op('Add student profile note','POST','/api/teacher/profile-notes',[
    F('studentId','Student ID','text',true),F('subjectId','Subject ID','text',true),
    F('achievements','Achievements','textarea'),F('behaviour','Behavior notes','textarea')]),
   op('Read student profile (ID lookup)','GET','/api/teacher/profile/:studentId',[
    F('studentId','Student ID','text',true)])]
 },
 teacherAssignments: {
  title:'Assignments',description:'Teachers can upload documents or save links and access assignments in assigned subjects.',access:'teacher',
  actions:[op('Upload an assignment document','UPLOAD','/api/teacher/assignments',[
    F('title','Assignment title','text',true),F('description','Description','textarea'),
    F('gradeNumber','Grade number','number',true,{min:1,max:13}),F('subjectId','Subject ID','text',true),
    F('file','PDF, DOCX, TXT, PNG or JPG (max 10MB)','file',true)]),
   op('Add an assignment link','POST','/api/teacher/assignment-links',[
    F('title','Assignment title','text',true),F('description','Description','textarea'),
    F('gradeNumber','Grade number','number',true,{min:1,max:13}),F('subjectId','Subject ID','text',true),
    F('linkUrl','HTTPS link','url',true)]),
   op('List assignments in my grade/subject','GET','/api/teacher/assignments',[
    F('grade','Grade','number',true,{min:1,max:13}),F('subjectId','Subject ID','text',true)],{query:['grade','subjectId']}),
   op('Download an assignment','DOWNLOAD','/api/teacher/assignments/:id/download',[
    F('id','Assignment ID','number',true,{min:1})])]
 }
};
// These additional pages reuse existing backend APIs. They do not create/rename any Java classes.
GROUPS.teachers = {
  title:'Teacher directory',description:'View teachers registered in the existing Teacher module. Create logins under Accounts & permissions.',access:'admin',
  actions:[op('View registered teachers','GET','/api/teachers'),
    op('Register a teacher record','POST','/api/teachers',[
      F('teacherId','Teacher ID','text',true),F('teacherName','Teacher name','text',true),
      F('nic','NIC number','text'),F('address','Address','textarea')],
      {helper:'This form uses common Teacher entity fields. Your current Teacher.java attributes were not provided, so check their names if your existing API rejects registration.'})]
};
GROUPS.students.actions.unshift(op('View registered students','GET','/api/students'));
GROUPS.grades = {title:'Grade records',description:'View grades in your existing Grade class and its repository.',access:'admin',actions:[op('View grade records','GET','/api/grades')]};
GROUPS.nonAcademic = {title:'Non-academic staff',description:'View staff information from the existing NonAcademic module.',access:'admin',actions:[op('View non-academic staff','GET','/api/non-academic')]};
GROUPS.assignmentMarks = {title:'Assignment marks',description:'Read records managed by your existing AssignmentMarks module.',access:'admin',actions:[op('View assignment marks','GET','/api/assignment-marks')]};
GROUPS.profileRecords = {title:'Student profiles',description:'Look up full profiles from the secure SMS portal.',access:'admin',actions:[op('Find student profile by ID','GET','/api/admin/students/:studentId/profile',[F('studentId','Student ID','text',true)])]};
GROUPS.overview.title='Overview';
GROUPS.overview.actions = [op('Refresh the overview','GET','/api/admin/dashboard'),op('Active student enrollments','GET','/api/admin/enrollments'),op('Teacher assignments','GET','/api/admin/scopes')];

const NAV = [
 {label:'MAIN',items:[['overview','Dashboard','overview']]},
 {label:'PEOPLE',access:'admin',items:[['students','Students','students'],['teachers','Teachers','teachers']]},
 {label:'STUDENTS',access:'admin',items:[['grades','Student grades','grades'],['profileRecords','Student profiles','profile']]},
 {label:'STAFF',access:'admin',items:[['nonAcademic','Non-academic staff','staff']]},
 {label:'RESOURCES',access:'admin',items:[['properties','Physical properties','properties'],['assignmentsAdmin','Teacher uploads','assignment']]},
 {label:'ACADEMIC',access:'admin',items:[['academic','Academic records','academic'],['assignmentMarks','Assignment marks','marks']]},
 {label:'ADMISSIONS',access:'admin',items:[['grade1','Grade 1 admissions','grade1'],['intermediate','Intermediate admissions','intermediate'],['al','A/L admissions','al']]},
 {label:'SYSTEM',access:'admin',items:[['accounts','Accounts & permissions','accounts']]},
 {label:'TEACHER WORK',items:[['teacherHome','Assigned work','teacher'],['teacherAttendance','Attendance','attendance'],['teacherMarks','Subject marks','marks'],['teacherProfile','Student profile updates','profile'],['teacherAssignments','Assignments','assignment']]}
];
const ICONS={overview:'◫',students:'♙',teachers:'♧',accounts:'⚿',academic:'▥',profile:'▤',grades:'▦',staff:'♧',properties:'▣',grade1:'①',intermediate:'②',al:'③',teacher:'▧',attendance:'▦',marks:'▥',assignment:'▰'};
const LABELS={registeredStudents:'Registered students',teacherAccounts:'Teacher accounts',propertyUnits:'Property units',grade1Applications:'Grade 1 applications',grade1Selected:'Grade 1 selected',enrolledByGrade:'Students by grade',intermediateApplications:'Intermediate admissions',alByStream:'A/L admissions',studentId:'Student ID',subjectId:'Subject ID',teacherCode:'Teacher ID',studentName:'Student name',gradeNumber:'Grade',grade_number:'Grade',student_id:'Student ID',seat_limit:'Seat limit',stream_name:'Stream',application_id:'Application ID',admission_id:'Admission ID',teacher_id:'Teacher ID',submitted_at:'Applied on',created_at:'Created on',teacherName:'Teacher name',term_name:'Term',marked_by:'Recorded by'};
const PRIVATE=/password|passwd|secret|token|hash|authorization|csrf|privatekey/i;
const ACTION_MAP={overview:0,accounts:0,students:0,teachers:0,properties:1,grade1:3,intermediate:3,al:4,assignmentsAdmin:0,teacherHome:0,grades:0,nonAcademic:0,assignmentMarks:0};

function node(tag,klass,text){const el=document.createElement(tag);if(klass)el.className=klass;if(text!==undefined)el.textContent=String(text);return el;}
function child(parent,...children){children.filter(Boolean).forEach(c=>parent.append(c));return parent;}
function prettyName(key){return LABELS[key]||String(key).replace(/([a-z\d])([A-Z])/g,'$1 $2').replaceAll('_',' ').replace(/^./,c=>c.toUpperCase()).replace(/\bid\b/gi,'ID');}
function display(value){if(value===null||value===undefined||value==='')return '—';if(typeof value==='boolean')return value?'Yes':'No';if(typeof value==='number')return Number.isFinite(value)?value.toLocaleString():'—';if(Array.isArray(value))return `${value.length} entries`;if(typeof value==='object')return Object.keys(value).filter(k=>!PRIVATE.test(k)).slice(0,3).map(k=>`${prettyName(k)}: ${display(value[k])}`).join(' · ')||'Details';return String(value);}
function isObject(v){return Boolean(v)&&typeof v==='object'&&!Array.isArray(v);}
function hasAdmin(){return !!state.user?.roles?.includes('ROLE_ADMIN');}
function moduleAllowed(key){return key==='overview'||hasAdmin()||GROUPS[key]?.access==='teacher';}
function toast(message,isError=false){const el=$('toast');el.textContent=message;el.className='toast'+(isError?' error':'');el.hidden=false;clearTimeout(toast.timer);toast.timer=setTimeout(()=>el.hidden=true,5100);}
function notice(message,type='info'){const area=$('notice-area');area.replaceChildren();if(!message)return;area.append(node('div','notice '+type,message));}
function connection(online){const chip=$('connection-chip');chip.classList.toggle('offline',!online);chip.lastChild.textContent=online?' API connected':' API unavailable';}
async function csrf(){const response=await fetch('/api/auth/csrf',{credentials:'same-origin',cache:'no-store'});if(!response.ok)throw new Error('Cannot obtain a security token from Spring Boot.');state.csrf=await response.json();return state.csrf;}
async function request(path,options={}){
 const method=(options.method||'GET').toUpperCase();if(!state.csrf)await csrf();
 const headers={...(options.headers||{})};if(!['GET','HEAD','OPTIONS'].includes(method))headers[state.csrf.headerName||'X-XSRF-TOKEN']=state.csrf.token;
 if(options.json!==undefined)headers['Content-Type']='application/json';
 let response;
 try{response=await fetch(path,{method,headers,credentials:'same-origin',cache:'no-store',body:options.json!==undefined?JSON.stringify(options.json):options.formData});connection(true);}catch(e){connection(false);throw new Error('Cannot reach the Spring Boot server. Check that the application is running on port 8080.');}
 if(options.download){if(!response.ok)throw new Error(await readError(response));return response;}
 const contentType=response.headers.get('content-type')||'';
 let body={};try{body=response.status===204?{}:contentType.includes('json')?await response.json():await response.text();}catch{body={};}
 if(!response.ok){let message=isObject(body)?(body.message||body.detail||body.error||body.title):body;
   if(response.status===401&&state.user){logoutView();toast('Session expired. Please sign in again.',true);}
   if(response.status===403&&(!message||message==='Forbidden'))message='This account does not have permission for that action.';
   throw new Error(`${message||errorDefault} (HTTP ${response.status})`);}
 return body;
}
async function readError(response){try{const data=await response.json();return data.message||data.detail||data.error||`HTTP ${response.status}`;}catch{return `HTTP ${response.status}`;}}
function authMessage(msg,success=false){const el=$('auth-message');el.textContent=msg;el.className='auth-message'+(success?' success':'');}
function authTab(tab){const login=tab==='login';$('login-form').hidden=!login;$('setup-form').hidden=login;$('login-tab').classList.toggle('selected',login);$('setup-tab').classList.toggle('selected',!login);$('login-tab').setAttribute('aria-selected',String(login));$('setup-tab').setAttribute('aria-selected',String(!login));$('auth-title').textContent=login?'Welcome back':'First administrator';$('auth-subtitle').textContent=login?'Sign in to continue to your school workspace.':'Create your school’s first administrator account.';authMessage('');}
function logoutView(){state.user=null;state.csrf=null;$('auth-screen').hidden=false;$('app-shell').hidden=true;$('sidebar').classList.remove('open');$('sidebar-scrim').hidden=true;authTab('login');$('login-form').reset();$('setup-form').reset();}
function welcome(){const name=state.user?.username||'Administrator';$('user-name').textContent=name;$('user-avatar').textContent=name.charAt(0).toUpperCase();$('role-label').textContent=hasAdmin()?'ADMIN':'TEACHER';$('user-role').textContent=hasAdmin()?'School administrator':'Teacher account';$('auth-screen').hidden=true;$('app-shell').hidden=false;nav();openModule('overview');}
async function initial(){
 $('login-tab').addEventListener('click',()=>authTab('login'));
 $('setup-tab').addEventListener('click',()=>authTab('setup'));
 $('login-form').addEventListener('submit',login);
 $('setup-form').addEventListener('submit',bootstrap);
 $('logout-button').addEventListener('click',logout);
 $('menu-toggle').addEventListener('click',()=>{$('sidebar').classList.toggle('open');$('sidebar-scrim').hidden=!$('sidebar').classList.contains('open');});
 $('sidebar-scrim').addEventListener('click',()=>{$('sidebar').classList.remove('open');$('sidebar-scrim').hidden=true;});
 $('dashboard-action').addEventListener('click',()=>hasAdmin()?openAction('students',1):openModule('teacherMarks'));
 $('grades-see-all').addEventListener('click',()=>openModule(hasAdmin()?'students':'teacherHome'));
 $('open-al').addEventListener('click',()=>openModule('al'));
 $('refresh-records').addEventListener('click',()=>refreshList());
 $('record-search').addEventListener('input',e=>{state.query=e.target.value.toLowerCase().trim();state.page=0;renderRecords();});
 $('dialog-close').addEventListener('click',closeAction);$('dialog-cancel').addEventListener('click',closeAction);
 $('details-close').addEventListener('click',()=>$('details-dialog').close());$('details-done').addEventListener('click',()=>$('details-dialog').close());
 $('action-dialog').addEventListener('click',e=>{if(e.target===$('action-dialog'))closeAction();});
 $('details-dialog').addEventListener('click',e=>{if(e.target===$('details-dialog'))$('details-dialog').close();});
 $('action-form').addEventListener('submit',submitAction);
 try{await csrf();const user=await request('/api/auth/me').catch(()=>null);if(user?.roles?.length){state.user=user;welcome();}else{logoutView();await csrf();}
   try{const b=await request('/api/auth/bootstrap-status');if(!b.available){$('setup-tab').hidden=true;$('auth-subtitle').textContent='Sign in using the account created by your administrator.';}}
   catch{/* Existing admin login still works if optional setup-status check fails. */}
 }catch(e){logoutView();authMessage(e.message);}
}
async function login(event){event.preventDefault();const form=event.currentTarget;const submit=$('login-submit');submit.disabled=true;authMessage('');try{
  await csrf();const values=Object.fromEntries(new FormData(form));await request('/api/auth/login',{method:'POST',json:{username:values.username,password:values.password}});
  await csrf();state.user=await request('/api/auth/me');form.reset();welcome();toast(`Welcome, ${state.user.username}`);
 }catch(e){authMessage(e.message);form.elements.password.value='';}finally{submit.disabled=false;}}
async function bootstrap(event){event.preventDefault();const form=event.currentTarget;const submit=$('setup-submit');submit.disabled=true;authMessage('');try{
 const values=Object.fromEntries(new FormData(form));await csrf();await request('/api/auth/bootstrap',{method:'POST',headers:{'X-Bootstrap-Secret':values.setupSecret},json:{username:values.username,password:values.password}});
 form.reset();authTab('login');$('setup-tab').hidden=true;authMessage('Administrator created. Sign in using your new username and password.',true);
 }catch(e){authMessage(e.message);form.elements.password.value='';form.elements.setupSecret.value='';}finally{submit.disabled=false;}}
async function logout(){try{await csrf();await request('/api/auth/logout',{method:'POST'});}catch(e){toast(e.message,true);}finally{logoutView();try{await csrf();}catch{}}}

function nav(){const root=$('side-nav');root.replaceChildren();for(const group of NAV){if(group.access==='admin'&&!hasAdmin())continue;const wrapper=node('div','nav-group');wrapper.append(node('div','nav-caption',group.label));for(const [key,label,icon] of group.items){if(!moduleAllowed(key))continue;const b=node('button','nav-item');b.type='button';b.dataset.page=key;child(b,node('span','nav-icon',ICONS[icon]||'◧'),node('span','nav-text',label));b.addEventListener('click',()=>openModule(key));wrapper.append(b);}root.append(wrapper);}}
function sectionHeading(title,subtitle,eyebrow='SCHOOL WORKSPACE'){$('page-eyebrow').textContent=eyebrow;$('page-title').textContent=title;$('page-subtitle').textContent=subtitle;$('breadcrumb-current').textContent=title;if($('content-title'))$('content-title').textContent=title;}
function pageButtons(key){const holder=$('page-buttons');holder.replaceChildren();const refresh=node('button','btn btn-secondary','↻  Refresh');refresh.type='button';refresh.addEventListener('click',()=>key==='overview'?loadDashboard():refreshList());holder.append(refresh);
 if(key!=='overview'){const action=GROUPS[key].actions.find(a=>!['GET','NAV','DOWNLOAD'].includes(a.method));if(action){const button=node('button','btn btn-primary','＋  '+primaryCaption(action.title));button.addEventListener('click',()=>selectAction(action,key));holder.append(button);}}}
function primaryCaption(title){return title.length>27?'New / update record':title;}
function openModule(key){if(!moduleAllowed(key)){toast('Not allowed for this account.',true);return;}state.module=key;state.query='';state.data=null;state.tableRows=[];state.page=0;$('record-search').value='';$('sidebar').classList.remove('open');$('sidebar-scrim').hidden=true;notice('');for(const b of $('side-nav').querySelectorAll('[data-page]'))b.classList.toggle('active',b.dataset.page===key);
 if(key==='overview'){$('module-view').hidden=true;$('dashboard-view').hidden=false;sectionHeading('Dashboard','Use the cards below or the navigation menu to manage school records.','OVERVIEW');pageButtons(key);loadDashboard();return;}
 $('dashboard-view').hidden=true;$('module-view').hidden=false;const group=GROUPS[key];sectionHeading(group.title,group.description,'SCHOOL MANAGEMENT');pageButtons(key);$('module-intro-title').textContent=group.title;$('module-intro-description').textContent=group.description;$('module-intro-icon').textContent=ICONS[key]||'▧';$('records-heading').textContent=key==='teacherHome'?'Assigned grades & subjects':'Records & results';$('records-caption').textContent='Select an action to view or update information';renderActions(key);resetRecord('Choose an action on the right to view records or complete a form.');const i=ACTION_MAP[key];if(i!==undefined&&group.actions[i]&&group.actions[i].method==='GET'&&!(group.actions[i].fields||[]).length){execute(group.actions[i],{},false).catch(e=>showError(e.message));}}
function renderActions(key){const root=$('module-actions');root.replaceChildren();const group=GROUPS[key];for(const action of group.actions){const writing=!['GET','NAV','DOWNLOAD'].includes(action.method);const danger=action.method==='DELETE'||/disable/i.test(action.title);const b=node('button','action-item'+(writing?' write':'')+(danger?' danger':''));b.type='button';b.title=action.title;
 const icon=action.method==='GET'?'↗':action.method==='UPLOAD'?'⇧':action.method==='DOWNLOAD'?'↓':action.method==='DELETE'?'×':action.method==='NAV'?'↗':action.method==='MULTI'?'▦':'＋';
 child(b,node('span','action-item-icon',icon),node('span','action-item-text',action.title),node('span','action-item-arrow','›'));b.addEventListener('click',()=>selectAction(action,key));root.append(b);}}
function resetRecord(message){$('record-content').replaceChildren();$('record-content').append(node('div','empty-data',message));$('table-footer').replaceChildren();$('record-count').textContent='No records loaded';}
function showError(message){notice(message,'error');toast(message,true);}
function selectAction(action,key){if(!moduleAllowed(key)){toast('Not allowed for this account',true);return;}
 if(action.method==='NAV'){location.assign(action.url);return;}
 if(!action.fields?.length&&action.method!=='DELETE'){
  if(!['GET','DOWNLOAD'].includes(action.method)&&!confirm(`Do you want to run "${action.title}"?`))return;
  execute(action,{},true).catch(e=>showError(e.message));return;
 }
 openDialog(action);
}
function openAction(key,index){if(key!==state.module)openModule(key);const action=GROUPS[key]?.actions[index];if(action)selectAction(action,key);}
function openDialog(action){state.modalAction=action;$('dialog-title').textContent=action.title;$('dialog-description').textContent=action.helper||'Complete the fields below, then submit.';const fields=$('dialog-fields');fields.replaceChildren();
 for(const field of action.fields||[]){let control,container;
  if(field.type==='checkbox'){container=node('label','field checkbox-field');control=node('input');control.type='checkbox';control.name=field.name;child(container,control,node('span','',field.label));}
  else{container=node('label','field'+(field.type==='textarea'||field.type==='file'?' full':''));container.append(node('span','',field.label));if(field.type==='textarea'){control=node('textarea');control.rows=3;}else if(field.type==='select'){control=node('select');for(const val of field.options||[]){const option=node('option','',val==='true'?'Yes':val==='false'?'No':val);option.value=val;control.append(option);}}else{control=node('input');control.type=field.type;}
    control.name=field.name;control.required=!!field.required;if(field.min!==undefined)control.min=field.min;if(field.max!==undefined)control.max=field.max;if(field.step!==undefined)control.step=field.step;if(field.type==='password')control.autocomplete='new-password';if(field.type==='file')control.accept='.pdf,.docx,.txt,.png,.jpg,.jpeg';if(field.type==='text')control.placeholder=field.label;container.append(control);
  }
  fields.append(container);
 }
 const button=$('dialog-submit');button.textContent=action.method==='GET'?'View records →':action.method==='DELETE'?'Delete record':action.method==='DOWNLOAD'?'Download file ↓':action.method==='UPLOAD'?'Upload assignment ↑':'Save changes →';button.disabled=false;
 $('action-form').reset();$('action-dialog').showModal();const first=fields.querySelector('input,select,textarea');if(first)first.focus();
}
function closeAction(){if($('action-dialog').open)$('action-dialog').close();state.modalAction=null;}
function collectValues(action,form){const data=new FormData(form);const result={};for(const field of action.fields||[]){let value;if(field.type==='checkbox'){value=!!form.elements[field.name].checked;}else{value=data.get(field.name);}
 if(field.type==='file'){result[field.name]=value;continue;}
 if(field.type==='number'){value=value===''||value===null?null:Number(value);if(value!==null&&!Number.isFinite(value))throw new Error('Please enter a valid number.');}
 else if(field.type==='select'&&(value==='true'||value==='false'))value=value==='true';
 else if(typeof value==='string')value=value.trim()?value.trim():null;
 result[field.name]=value;
 }return result;}
function prepare(action,values){let path=action.url;const inPath=[];path=path.replace(/:([a-zA-Z][a-zA-Z0-9]*)/g,(_,name)=>{if(values[name]===null||values[name]===undefined||values[name]==='')throw new Error(`${prettyName(name)} is required`);inPath.push(name);return encodeURIComponent(String(values[name]));});const params=new URLSearchParams();for(const name of action.query||[]){if(values[name]!==null&&values[name]!==undefined&&values[name]!=='')params.append(name,String(values[name]));}if(params.size)path+='?'+params.toString();const body={...values};for(const name of [...inPath,...(action.query||[])])delete body[name];return {path,body};}
async function submitAction(event){event.preventDefault();const action=state.modalAction;if(!action)return;const button=$('dialog-submit');button.disabled=true;try{const values=collectValues(action,event.currentTarget);if((action.method==='DELETE'||/disable/i.test(action.title))&&!confirm(`Confirm: ${action.title}?`)){button.disabled=false;return;}await execute(action,values,true);closeAction();}catch(e){showError(e.message);}finally{button.disabled=false;}}
async function execute(action,values={},userAction=false){notice('');if(action.method==='MULTI'){
 const results=[];for(const streamName of streams){results.push(await request(`/api/admin/admissions/al/capacity/${streamName}`,{method:'PUT',json:{limit:values.limit}}));}renderData(results,'All stream capacities updated');notice('The capacity was updated for all four streams.','success');toast('A/L stream capacities updated');return;
 }
 const {path,body}=prepare(action,values);if(action.method==='DOWNLOAD'){
 const response=await request(path,{download:true});const blob=await response.blob();const url=URL.createObjectURL(blob);const anchor=node('a');anchor.href=url;const header=response.headers.get('content-disposition')||'';const name=header.match(/filename\*?=(?:UTF-8''|\")?([^";]+)/i);anchor.download=name?decodeURIComponent(name[1].replace(/"/g,'')):'assignment-file';document.body.append(anchor);anchor.click();anchor.remove();setTimeout(()=>URL.revokeObjectURL(url),60000);toast('Download started');return;}
 if(action.method==='UPLOAD'){const form=new FormData();for(const [key,val]of Object.entries(values)){if(val!==null&&val!==undefined)form.append(key,val);}const response=await request(path,{method:'POST',formData:form});renderData(response,action.title);notice(response?.message||'Assignment uploaded successfully.','success');toast('Assignment uploaded');return;}
 const method=action.method;const result=await request(path,{method,...(!['GET','HEAD'].includes(method)?{json:body}:{})});if(state.module==='overview'&&action.url==='/api/admin/dashboard'){renderDashboard(result);return;}
 renderData(result,action.title);if(method!=='GET'){notice(result?.message||'Action completed successfully.','success');toast('Saved successfully');}else if(userAction)toast('Records loaded');}
async function refreshList(){const key=state.module;if(key==='overview'){loadDashboard();return;}const group=GROUPS[key];const index=ACTION_MAP[key];const a=group.actions[index];if(a?.method==='GET'&&!(a.fields||[]).length){try{await execute(a,{},true);}catch(e){showError(e.message);}}else{resetRecord('Use the actions on the right to search or load records for this module.');}}
function renderData(payload,title){state.data=payload;state.query='';state.page=0;$('record-search').value='';const results=$('record-content');results.replaceChildren();$('table-footer').replaceChildren();
 // Single objects with a message are confirmations, not raw JSON dumps.
 if(isObject(payload)&&typeof payload.message==='string'){const block=node('div','record-message');child(block,node('span','result-badge','✓ Action completed'),node('h3','',payload.message));const details={...payload};delete details.message;const dl=detailsGrid(details);if(dl)block.append(dl);results.append(block);$('record-count').textContent='Action completed';state.tableRows=[];return;}
 if(Array.isArray(payload)){state.tableRows=payload;renderRecords();return;}
 if(isObject(payload)){
  const nested=Object.entries(payload).filter(([k,v])=>!PRIVATE.test(k)&&(Array.isArray(v)||isObject(v)));
  const scalars=Object.fromEntries(Object.entries(payload).filter(([k,v])=>!PRIVATE.test(k)&&!Array.isArray(v)&&!isObject(v)));
  if(Object.keys(scalars).length){const holder=node('div','record-message');holder.append(node('h3','',title||'Details'));holder.append(detailsGrid(scalars));results.append(holder);}
  for(const [key,val]of nested){const holder=node('div','record-message');holder.append(node('h3','',prettyName(key)));if(Array.isArray(val)&&val.every(isObject))holder.append(recordsTable(val));else if(isObject(val))holder.append(detailsGrid(val)||node('p','', 'No details'));else holder.append(node('div','',display(val)));results.append(holder);}
  if(!nested.length&&!Object.keys(scalars).length)results.append(node('div','empty-data','No data available.'));
  $('record-count').textContent='Details loaded';state.tableRows=[];return;
 }
 results.append(node('div','record-message',display(payload)));$('record-count').textContent='Response loaded';state.tableRows=[];
}
function visibleKeys(rows){const keys=[];for(const row of rows.slice(0,50))for(const key of Object.keys(row))if(!PRIVATE.test(key)&&!keys.includes(key))keys.push(key);return keys;}
function recordsTable(rows,doPaging=false){if(!rows.length)return node('div','empty-data','No records found yet.');const filtered=doPaging&&state.query?rows.filter(row=>Object.entries(row).some(([k,v])=>!PRIVATE.test(k)&&String(display(v)).toLowerCase().includes(state.query))):rows;
 if(!filtered.length)return node('div','empty-data','No records match your search.');const portion=doPaging?filtered.slice(state.page*PAGE_SIZE,(state.page+1)*PAGE_SIZE):filtered.slice(0,45);const keys=visibleKeys(portion).slice(0,7);const wrap=node('div','table-overflow');wrap.style.overflowX='auto';const table=node('table','data-table');const thead=node('thead');const hr=node('tr');for(const key of keys)hr.append(node('th','',prettyName(key)));hr.append(node('th','','Details'));thead.append(hr);table.append(thead);const tbody=node('tbody');for(const row of portion){const tr=node('tr');for(const key of keys){const cell=node('td','',display(row[key]));tr.append(cell);}const detailcell=node('td');const btn=node('button','row-detail','View →');btn.type='button';btn.addEventListener('click',()=>openDetails(row));detailcell.append(btn);tr.append(detailcell);tbody.append(tr);}table.append(tbody);wrap.append(table);
 if(doPaging){$('record-count').textContent=`${filtered.length} of ${rows.length} records`;
  const footer=$('table-footer');footer.replaceChildren();footer.append(node('span','',`Showing ${state.page*PAGE_SIZE+1}–${Math.min((state.page+1)*PAGE_SIZE,filtered.length)} of ${filtered.length}`));const pages=Math.ceil(filtered.length/PAGE_SIZE);if(pages>1){const pager=node('div','pager');const prev=node('button','','Previous');prev.disabled=state.page===0;prev.addEventListener('click',()=>{state.page--;renderRecords();});const next=node('button','','Next');next.disabled=state.page>=pages-1;next.addEventListener('click',()=>{state.page++;renderRecords();});child(pager,prev,node('span','',`${state.page+1} / ${pages}`),next);footer.append(pager);}}
 return wrap;
}
function renderRecords(){const root=$('record-content');root.replaceChildren();$('table-footer').replaceChildren();root.append(recordsTable(state.tableRows,true));}
function detailsGrid(info){const entries=Object.entries(info||{}).filter(([key])=>!PRIVATE.test(key));if(!entries.length)return null;const dl=node('dl','item-detail');for(const [key,value] of entries){const item=node('div','detail-row');child(item,node('dt','',prettyName(key)),node('dd','',display(value)));dl.append(item);}return dl;}
function openDetails(row){$('details-title').textContent='Record details';const content=$('details-content');content.replaceChildren();content.append(detailsGrid(row)||node('p','','No fields available.'));$('details-dialog').showModal();}
async function loadDashboard(){try{
 if(hasAdmin()){const data=await request('/api/admin/dashboard');renderDashboard(data);}else{const scopes=await request('/api/teacher/my-scopes');renderTeacherDashboard(scopes);}
 }catch(e){showError(e.message);$('metrics-grid').replaceChildren();}}
// Dashboard module cards deliberately preserve all the existing GROUPS actions.
// A dash means the dashboard API did not supply a count. It never means zero.
const ADMIN_CARDS = [
 ['students','Students','Register and manage student details, addresses and contacts.'],
 ['teachers','Teachers','View existing teachers and register teacher records.'],
 ['grades','Student grades','Manage grade records and student enrollments.'],
 ['profileRecords','Student profiles','Look up student details, achievements and behavior.'],
 ['nonAcademic','Non-academic staff','View non-academic employees and staff information.'],
 ['properties','Physical properties','Manage school equipment and property quantities.'],
 ['teacherAttendance','Attendance','Record and view student attendance by class and subject.'],
 ['teacherAssignments','Teacher assignments','Upload assignments and manage teacher resources.'],
 ['assignmentMarks','Assignment marks','Read marks for submitted student assignments.'],
 ['teacherMarks','Subject marks','Record marks for subjects and assigned students.'],
 ['grade1','Grade 1 admissions','Record all applicants and choose the first N students.'],
 ['intermediate','Intermediate admissions','Manage vacancies, transfers and Grade 6 scholarships.'],
 ['al','A/L admissions','Set stream capacities and prioritize school students.'],
 ['academic','Academic records','Find student marks using student and subject IDs.'],
 ['accounts','Accounts & permissions','Register teacher accounts and manage access rights.'],
 ['assignmentsAdmin','Uploaded assignments','View and download assignments uploaded by teachers.'],
 ['teacherProfile','Profile updates','Add achievements and behavior to student profiles.']
];
const TEACHER_CARDS = [
 ['teacherHome','My assigned classes','See your permitted grades, subjects and students.'],
 ['teacherAttendance','Attendance','Record attendance for the students you teach.'],
 ['teacherMarks','Subject marks','Enter student marks within your assigned subjects.'],
 ['teacherProfile','Student profiles','Add student achievements and behavior notes.'],
 ['teacherAssignments','Assignments','Upload assignment files, add links and download work.']
];
function simpleCount(value){
 if(value===undefined||value===null||Number.isNaN(Number(value)))return '—';
 return Number(value).toLocaleString();
}
function sumCounts(value,keys=['applications','application_count','student_count','selected']){
 if(typeof value==='number')return value;
 if(!Array.isArray(value))return null;
 let total=0,hasCount=false;
 for(const row of value){for(const key of keys){if(row&&Number.isFinite(Number(row[key]))&&row[key]!==null&&row[key]!==undefined){total+=Number(row[key]);hasCount=true;break;}}}
 return hasCount?total:null;
}
function moduleCard(key,title,description,value){
 const card=node('article','module-card');card.dataset.module=key;
 const heading=node('div','module-card-top');
 heading.append(node('h3','module-card-title',title));
 heading.append(node('strong','module-card-count',simpleCount(value)));
 card.append(heading);
 card.append(node('p','module-card-description',description));
 const btn=node('button','module-card-open','Open');btn.type='button';
 btn.setAttribute('aria-label','Open '+title);btn.addEventListener('click',()=>openModule(key));
 card.append(btn);
 return card;
}
function renderDashboard(data){
 const root=$('metrics-grid');root.replaceChildren();
 const gradeRows=Array.isArray(data.enrolledByGrade)?data.enrolledByGrade:[];
 const counts={
  students:data.registeredStudents,
  accounts:data.teacherAccounts,
  properties:data.propertyUnits,
  grade1:data.grade1Applications,
  grades:sumCounts(gradeRows,['student_count','count','students']),
  intermediate:sumCounts(data.intermediateApplications),
  al:sumCounts(data.alByStream,['applications','application_count']),
  teachers:null,profileRecords:null,nonAcademic:null,
  teacherAttendance:null,teacherAssignments:null,
  assignmentMarks:null,teacherMarks:null,academic:null,
  assignmentsAdmin:null,teacherProfile:null
 };
 for(const [key,title,description] of ADMIN_CARDS)root.append(moduleCard(key,title,description,counts[key]));
 $('welcome-title').textContent=`Welcome back, ${state.user?.username||'Administrator'}.`;
 $('welcome-caption').textContent='Choose a module below to manage your school.';
 $('dashboard-action').textContent='Register a student +';
 $('grades-see-all').hidden=false;$('open-al').hidden=false;$('stream-panel').hidden=false;
 $('dashboard-section-title').textContent='Students by grade';
 const gradeRoot=$('grade-overview');gradeRoot.replaceChildren();
 if(!gradeRows.length)gradeRoot.append(node('div','empty-data','No active grade enrollment records yet.'));
 else{
  const max=Math.max(1,...gradeRows.map(r=>Number(r.student_count)||0));
  for(const row of gradeRows){const count=Number(row.student_count)||0;
   const line=node('div','grade-bar-line');const track=node('div','grade-track');const fill=node('div','grade-fill');
   fill.style.width=`${Math.max(3,count/max*100)}%`;track.append(fill);
   child(line,node('span','',`Grade ${row.grade_number}`),track,node('strong','grade-count',count));gradeRoot.append(line);}
 }
 const al=$('stream-overview');al.replaceChildren();const stream=Array.isArray(data.alByStream)?data.alByStream:[];
 for(const row of stream){const c=node('div','stream-card');
  child(c,node('strong','',prettyName(row.stream_name)),node('div','stream-number',row.selected??0),node('div','stream-sub',`${row.applications??0} applications · ${row.seat_limit??0} seats`));al.append(c);}
 if(!stream.length)al.append(node('div','empty-data','No A/L admission records yet.'));
 renderQuickActions();
}
function renderTeacherDashboard(scopes){
 const data=Array.isArray(scopes)?scopes:[];
 $('welcome-title').textContent=`Welcome back, ${state.user?.username||'Teacher'}.`;
 $('welcome-caption').textContent='Open a teaching module below to continue your work.';
 $('dashboard-action').textContent='Enter subject marks +';
 $('grades-see-all').hidden=false;$('open-al').hidden=true;$('stream-panel').hidden=true;
 const root=$('metrics-grid');root.replaceChildren();
 for(const [key,title,description] of TEACHER_CARDS)root.append(moduleCard(key,title,description,key==='teacherHome'?data.length:null));
 const gradeRoot=$('grade-overview');gradeRoot.replaceChildren();
 $('dashboard-section-title').textContent='Your assigned teaching scopes';
 if(!data.length)gradeRoot.append(node('div','empty-data','No grade and subject assignments yet. Ask your administrator to assign your work.'));
 else gradeRoot.append(recordsTable(data));
 renderQuickActions();
}
function renderQuickActions(){const root=$('quick-actions');root.replaceChildren();const choices=hasAdmin()?[
 ['Register a student','Add a student to the school','students',1,'＋'],['Register a teacher login','Give a teacher portal access','accounts',1,'♧'],['Grade 1 admissions','Set capacity and choose applicants','grade1',0,'①'],['View student marks','Search by student and subject','academic',0,'▥']]:[
 ['Mark attendance','Record presence for a student','teacherAttendance',0,'▦'],['Enter subject marks','Save student marks','teacherMarks',0,'▥'],['Upload assignment','Upload a document or link','teacherAssignments',0,'⇧'],['Add a student profile note','Achievements and behavior','teacherProfile',0,'▤']];for(const [title,subtitle,key,index,icon]of choices){const b=node('button','quick-action');b.type='button';child(b,node('span','quick-action-icon',icon),child(node('span','quick-action-text'),node('strong','',title),node('small','',subtitle)),node('span','quick-arrow','→'));b.addEventListener('click',()=>openAction(key,index));root.append(b);}}
document.addEventListener('DOMContentLoaded',initial);
