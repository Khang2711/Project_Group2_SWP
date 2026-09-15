<%@page pageEncoding="UTF-8"%>
<%@page import="java.util.*,controller.ManagementServlet.Spec,controller.ManagementServlet.Field"%>
<%@include file="/WEB-INF/include/header.jsp"%>
<% Spec spec=(Spec)request.getAttribute("spec");Map<String,Object> rec=(Map<String,Object>)request.getAttribute("record");boolean editing=rec!=null; %>
<section class="panel"><span class="eyebrow"><%="AD".equals(hr)?"SYSTEM ADMIN":"CINEMA MANAGER"%></span><h1 class="page-title"><%=Html.e(spec.title)%></h1>
<%if(request.getAttribute("error")!=null){%><p class="alert alert-error"><%=Html.e(request.getAttribute("error"))%></p><%}%>
<%if(request.getParameter("saved")!=null){%><p class="alert alert-success" role="status">Đã lưu thay đổi.</p><%}%>
<form accept-charset="UTF-8" method="get" class="search-form"><input aria-label="Tìm theo mã" name="q" value="<%=Html.e(request.getParameter("q"))%>" placeholder="Tìm theo mã"><button class="btn btn-soft">Tìm kiếm</button></form>
<details <%=editing?"open":""%>><summary class="btn btn-primary"><%=editing?"Chỉnh sửa bản ghi":"Thêm bản ghi"%></summary>
<form accept-charset="UTF-8" method="post"><input type="hidden" name="csrf" value="<%=session.getAttribute("CSRF")%>"><input type="hidden" name="mode" value="<%=editing?"update":"create"%>"><div class="form-grid">
<% for(int i=0;i<spec.fields.length;i++){Field f=spec.fields[i];Object raw=editing?rec.get(f.key):"";String v=raw==null?"":raw.toString();if(raw instanceof Boolean)v=(Boolean)raw?"1":"0";%>
<label><%=Html.e(f.label)%>
<%if(f.type.equals("select")){%><select name="<%=f.key%>"><%for(String option:f.options){String[]o=option.split(":",2);%><option value="<%=o[0]%>" <%=o[0].equals(v)?"selected":""%>><%=Html.e(o[1])%></option><%}%></select>
<%}else if(f.type.equals("cinema")||f.type.equals("room")){String id=f.type.equals("cinema")?"cinemaID":"roomID",label=f.type.equals("cinema")?"cinemaName":"roomName";List<Map<String,Object>>options=(List<Map<String,Object>>)request.getAttribute(f.type.equals("cinema")?"cinemas":"rooms");%>
<select name="<%=f.key%>"><option value="">Chọn…</option><%if(options!=null)for(Map<String,Object>o:options){%><option value="<%=Html.e(o.get(id))%>" <%=v.equals(String.valueOf(o.get(id)))?"selected":""%>><%=Html.e(o.get(label))%> · <%=Html.e(o.get(id))%></option><%}%></select>
<%}else{%><input type="<%=f.type%>" name="<%=f.key%>" value="<%=Html.e(v)%>" <%=editing&&i==0?"readonly":""%> <%=f.type.equals("number")?"min=0 step=1":""%>><%}%></label><%}%>
<%if(spec.table.equals("tblUsers")&&!editing){%><label>Mật khẩu ban đầu<input type="password" name="password" minlength="8" required autocomplete="new-password"></label><%}%>
</div><p><button class="btn btn-primary">Lưu thay đổi</button> <a class="btn btn-soft" href="<%=headerCtx+request.getServletPath()%>">Hủy</a></p></form></details>
<div class="table-wrap" style="margin-top:24px"><table><thead><tr><%for(Field f:spec.fields){%><th><%=Html.e(f.label)%></th><%}%><th>Thao tác</th></tr></thead><tbody>
<%List<Map<String,Object>> rows=(List<Map<String,Object>>)request.getAttribute("records");if(rows!=null)for(Map<String,Object> row:rows){%><tr><%for(Field f:spec.fields){%><td><%=Html.e(row.get(f.key))%></td><%}%><td><a class="btn btn-soft" href="?edit=<%=java.net.URLEncoder.encode(String.valueOf(row.get(spec.fields[0].key)),"UTF-8")%>">Sửa</a></td></tr><%}%>
<%if(rows==null||rows.isEmpty()){%><tr><td colspan="<%=spec.fields.length+1%>">Chưa có dữ liệu phù hợp.</td></tr><%}%></tbody></table></div></section>
<%@include file="/WEB-INF/include/footer.jsp"%>
