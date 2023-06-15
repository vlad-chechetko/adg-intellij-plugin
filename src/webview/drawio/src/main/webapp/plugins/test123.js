Draw.loadPlugin(function(ui)
{
	mxResources.parse('test123=Test123...');

	ui.actions.addAction('test123', function()
	{
		var dlg = new EmbedDialog(ui, "Hello", null, null, null, 'Test123:');
		ui.showDialog(dlg.container, 400, 400, true, true);
		dlg.init();
	});
	
	var menu = ui.menus.get('extras');
	var oldFunct = menu.funct;
	
	menu.funct = function(menu, parent)
	{
		oldFunct.apply(this, arguments);
		ui.menus.addMenuItems(menu, ['-', 'test123'], parent);
	};
});
