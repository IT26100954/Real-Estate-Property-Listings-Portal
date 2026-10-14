let csrf='';
export async function api(path,options={}) {
 const headers={'Content-Type':'application/json',...(options.headers||{})};
 if(options.method&&options.method!=='GET')headers['X-CSRF-Token']=csrf;
 const response=await fetch('/api'+path,{...options,headers,credentials:'same-origin'});
 const data=await response.json();
 if(!response.ok)throw new Error(data.message||`Request failed (${response.status})`);
 if(data.csrf)csrf=data.csrf;
 return data;
}
export function initialize(){return api('/auth/me');}
